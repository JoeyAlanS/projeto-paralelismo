package com.unifor.concorrente;

import org.jocl.*;
import static org.jocl.CL.*;

public class BuscadorParaleloGPU {
    // Código em C OpenCL/GPU
    private static final String KERNEL_SRC =
            "__kernel void contar_palavra(__global const char* texto, int textoLen, " +
                    "                             __global const char* palavra, int palavraLen, " +
                    "                             __global int* resultado) {" +
                    "    int gid = get_global_id(0);" +
                    "    if (gid + palavraLen > textoLen) return;" +
                    "    " +
                    "    bool igual = true;" +
                    "    for (int i = 0; i < palavraLen; i++) {" +
                    "        if (texto[gid + i] != palavra[i]) {" +
                    "            igual = false;" +
                    "            break;" +
                    "        }" +
                    "    }" +
                    "    " +
                    "    if (igual) {" +
                    "        // Validação de fronteira para garantir palavra inteira isolada\n" +
                    "        if (gid > 0) {" +
                    "            char p = texto[gid - 1];" +
                    "            if ((p >= 'a' && p <= 'z') || (p >= 'A' && p <= 'Z') || (p >= '0' && p <= '9')) {" +
                    "                igual = false;" +
                    "            }" +
                    "        }" +
                    "        if (gid + palavraLen < textoLen) {" +
                    "            char n = texto[gid + palavraLen];" +
                    "            if ((n >= 'a' && n <= 'z') || (n >= 'A' && n <= 'Z') || (n >= '0' && n <= '9')) {" +
                    "                igual = false;" +
                    "            }" +
                    "        }" +
                    "    }" +
                    "    " +
                    "    if (igual) {" +
                    "        atomic_inc(resultado);" + // Operação atômica segura na GPU
                    "    }" +
                    "}";

    public static int contar(String texto, String palavraAlvo) {
        // Normalização
        String textoMinusculo = texto.toLowerCase();
        String palavraMinuscula = palavraAlvo.toLowerCase();

        byte[] textoBytes = textoMinusculo.getBytes();
        byte[] palavraBytes = palavraMinuscula.getBytes();

        int textoLen = textoBytes.length;
        int palavraLen = palavraBytes.length;
        int[] resultadoArray = new int[]{0};

        CL.setExceptionsEnabled(true);

        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        cl_platform_id platforms[] = new cl_platform_id[numPlatformsArray[0]];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
        cl_device_id devices[] = new cl_device_id[numDevicesArray[0]];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, devices.length, devices, null);
        cl_device_id device = devices[0];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        cl_queue_properties queueProperties = new cl_queue_properties();
        cl_command_queue commandQueue = clCreateCommandQueueWithProperties(context, device, queueProperties, null);

        // Alocação de Buffers na VRAM
        cl_mem memTexto = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * textoLen, Pointer.to(textoBytes), null);
        cl_mem memPalavra = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_char * palavraLen, Pointer.to(palavraBytes), null);
        cl_mem memResultado = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int, Pointer.to(resultadoArray), null);

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{KERNEL_SRC}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        cl_kernel kernel = clCreateKernel(program, "contar_palavra", null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memTexto));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{textoLen}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memPalavra));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{palavraLen}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memResultado));

        long globalWorkSize[] = new long[]{textoLen};

        // Disparo paralelo massivo na GPU
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, globalWorkSize, null, 0, null, null);
        clEnqueueReadBuffer(commandQueue, memResultado, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(resultadoArray), 0, null, null);

        // Liberação explícita de memória nativa (C/C++) evitam vazamento de VRAM
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseMemObject(memTexto);
        clReleaseMemObject(memPalavra);
        clReleaseMemObject(memResultado);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return resultadoArray[0];
    }
}