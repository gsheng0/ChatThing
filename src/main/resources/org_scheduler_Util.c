#include <jni.h>
#include "org_scheduler_Util.h"
#include <libheif/heif.h>
#include <jpeglib.h>
#include <stdlib.h>
#include <string.h>

// ---- JPEG Memory Destination ----
struct mem_destination_mgr {
    struct jpeg_destination_mgr pub;
    unsigned char **outbuffer;
    size_t *outsize;
    JOCTET *buffer;
    size_t bufsize;
};

static void init_destination(j_compress_ptr cinfo) {
    struct mem_destination_mgr *dest = (struct mem_destination_mgr *)cinfo->dest;
    dest->buffer = (JOCTET *)malloc(dest->bufsize);
    dest->pub.next_output_byte = dest->buffer;
    dest->pub.free_in_buffer = dest->bufsize;
}

static boolean empty_output_buffer(j_compress_ptr cinfo) {
    struct mem_destination_mgr *dest = (struct mem_destination_mgr *)cinfo->dest;
    size_t newsize = dest->bufsize * 2;
    JOCTET *newbuffer = (JOCTET *)realloc(dest->buffer, newsize);
    if (!newbuffer) return FALSE;
    dest->pub.next_output_byte = newbuffer + (dest->bufsize - dest->pub.free_in_buffer);
    dest->pub.free_in_buffer += (newsize - dest->bufsize);
    dest->buffer = newbuffer;
    dest->bufsize = newsize;
    return TRUE;
}

static void term_destination(j_compress_ptr cinfo) {
    struct mem_destination_mgr *dest = (struct mem_destination_mgr *)cinfo->dest;
    *dest->outbuffer = dest->buffer;
    *dest->outsize = dest->bufsize - dest->pub.free_in_buffer;
}

static void jpeg_mem_dest_custom(j_compress_ptr cinfo, unsigned char **outbuffer, size_t *outsize) {
    struct mem_destination_mgr *dest;
    if (cinfo->dest == NULL) {
        cinfo->dest = (struct jpeg_destination_mgr *)(*cinfo->mem->alloc_small)(
            (j_common_ptr)cinfo, JPOOL_PERMANENT, sizeof(struct mem_destination_mgr));
    }
    dest = (struct mem_destination_mgr *)cinfo->dest;
    dest->pub.init_destination = init_destination;
    dest->pub.empty_output_buffer = empty_output_buffer;
    dest->pub.term_destination = term_destination;
    dest->outbuffer = outbuffer;
    dest->outsize = outsize;
    dest->bufsize = 4096;
}

// ---- JNI Function ----
JNIEXPORT jbyteArray JNICALL Java_org_scheduler_Util_convertHeicBytesToJpegBytes
  (JNIEnv *env, jobject obj, jbyteArray heicData) {

    jbyte *heicBytes = (*env)->GetByteArrayElements(env, heicData, NULL);
    jsize heicSize = (*env)->GetArrayLength(env, heicData);

    struct heif_context* ctx = heif_context_alloc();
    struct heif_error err = heif_context_read_from_memory_without_copy(ctx, heicBytes, heicSize, NULL);
    if (err.code != heif_error_Ok) {
        heif_context_free(ctx);
        (*env)->ReleaseByteArrayElements(env, heicData, heicBytes, JNI_ABORT);
        return NULL;
    }

    struct heif_image_handle* handle;
    heif_context_get_primary_image_handle(ctx, &handle);

    struct heif_image* img;
    heif_decode_image(handle, &img, heif_colorspace_RGB, heif_chroma_interleaved_RGB, NULL);

    int width = heif_image_get_width(img, heif_channel_interleaved);
    int height = heif_image_get_height(img, heif_channel_interleaved);
    const uint8_t* data = heif_image_get_plane_readonly(img, heif_channel_interleaved, NULL);

    unsigned char *jpegBuf = NULL;
    size_t jpegSize = 0;

    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;
    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);
    jpeg_mem_dest_custom(&cinfo, &jpegBuf, &jpegSize);

    cinfo.image_width = width;
    cinfo.image_height = height;
    cinfo.input_components = 3;
    cinfo.in_color_space = JCS_RGB;
    jpeg_set_defaults(&cinfo);
    jpeg_start_compress(&cinfo, TRUE);

    int row_stride = width * 3;
    while (cinfo.next_scanline < cinfo.image_height) {
        JSAMPROW row_pointer = (JSAMPROW)&data[cinfo.next_scanline * row_stride];
        jpeg_write_scanlines(&cinfo, &row_pointer, 1);
    }

    jpeg_finish_compress(&cinfo);
    jpeg_destroy_compress(&cinfo);

    jbyteArray outArray = (*env)->NewByteArray(env, jpegSize);
    (*env)->SetByteArrayRegion(env, outArray, 0, jpegSize, (jbyte*)jpegBuf);

    free(jpegBuf);
    heif_image_release(img);
    heif_image_handle_release(handle);
    heif_context_free(ctx);

    (*env)->ReleaseByteArrayElements(env, heicData, heicBytes, JNI_ABORT);

    return outArray;
}
