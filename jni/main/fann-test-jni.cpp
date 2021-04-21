#include <math.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "fann.h"
#include "floatfann.h"

#define  LOG_TAG "FANN TEST"
#define  LOG(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

using namespace std;

extern "C" {
JNIEXPORT jstring JNICALL Java_com_gmmh_mindwriter_MainActivity_testFann(JNIEnv* jenv,
		jclass,jint entero,jstring entrenamientox, jfloat delta, jfloat lowAlpha, jfloat highAlpha, jfloat lowBeta, jfloat highBeta, jfloat lowGamma, jfloat midGamma, jfloat theta) {
		LOG("ENTERO: %d",entero);
		const char *cadenaConvertida = jenv->GetStringUTFChars(entrenamientox, JNI_FALSE);
		LOG("CADENA ENTRENAMIENTO: %s",cadenaConvertida);
/*
		//Para entrenar y generar archivo
		FILE * archivo = fopen(cadenaConvertida,"a");
		if (archivo != NULL)
			fprintf(archivo,"%s",cadenaConvertida);
		else
        	LOG("Error de apertura del archivo :( ");
		fclose(archivo);


		const unsigned int num_input = 8;
		const unsigned int num_output = 5;

		//Cambiar por el # de capas
		const unsigned int num_layers = 3;

		//Cambiar por el # de neuronas ocultas
		const unsigned int num_neurons_hidden = 3;

		const float desired_error = (const float) 0.001;
		const unsigned int max_epochs = 10000;
		const unsigned int epochs_between_reports = 10000;
		struct fann *ann = fann_create_standard(num_layers, num_input,num_neurons_hidden, num_output);
		fann_set_activation_function_hidden(ann, FANN_SIGMOID_SYMMETRIC);
		fann_set_activation_function_output(ann, FANN_SIGMOID_SYMMETRIC);
		fann_train_on_file(ann,cadenaConvertida, max_epochs,
		epochs_between_reports, desired_error);
		fann_save(ann, "/storage/sdcard0/Download/rna.net");
		return 1;
*/


		fann_type inputy[8]={(float)delta,(float)lowAlpha, (float)highAlpha, (float)lowBeta,(float)highBeta, (float)lowGamma, (float)midGamma, (float) theta};
		struct fann *anny = fann_create_from_file(cadenaConvertida);
		fann_type *calc_outy = fann_run(anny,inputy);
		LOG("Prueba (%f,%f,%f,%f,%f,%f,%f,%f) -> %f\n", inputy[0], inputy[1],inputy[2],inputy[3],inputy[4],inputy[5],inputy[6],inputy[7], calc_outy[0]);

		LOG("[RESULTADO FLOAT] 0: %.8f",calc_outy[0]);
		LOG("[RESULTADO FLOAT] 1: %.8f",calc_outy[1]);
		LOG("[RESULTADO FLOAT] 2: %.8f",calc_outy[2]);
		LOG("[RESULTADO FLOAT] 3: %.8f",calc_outy[3]);
		LOG("[RESULTADO FLOAT] 4: %.8f",calc_outy[4]);

		int resultado=calc_outy[0] > 0.0505?1:0;
		int resultado_1=calc_outy[1] > 0?1:0;
		int resultado_2=calc_outy[2] > 0?1:0;
		int resultado_3=calc_outy[3] > 0?1:0;
		int resultado_4=calc_outy[4] > 0?1:0;

		LOG("[RESULTADO ENTERO] 0: %d",resultado);
		LOG("[RESULTADO ENTERO] 1: %d",resultado_1);
		LOG("[RESULTADO ENTERO] 2: %d",resultado_2);
		LOG("[RESULTADO ENTERO] 3: %d",resultado_3);
		LOG("[RESULTADO ENTERO] 4: %d",resultado_4);
		char aux_final[6];
		sprintf(aux_final, "%d%d%d%d%d", resultado,resultado_1,resultado_2,resultado_3,resultado_4);
		LOG("STRING: %s",aux_final);
		jstring jfinal =jenv->NewStringUTF(aux_final);
		fann_destroy(anny);
		//return resultado;
		return jfinal;
	}
}
