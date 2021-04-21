#include <stdio.h>
#include "floatfann.h"
int main()
{
    fann_type *calc_out;
    fann_type input[8]={0,0,0,1,1,0,1,1};
    struct fann *ann = fann_create_from_file("xor_float.net");
    int i=0;
    for(i=0;i<8;i=i+2)
    {
	   fann_type aux[2]={input[i],input[i+1]};
        calc_out = fann_run(ann, aux);
	   printf("Prueba XOR (%f,%f) -> %f\n", input[i], input[i+1], calc_out[0]);
    }
    fann_destroy(ann);
    return 0;
}
