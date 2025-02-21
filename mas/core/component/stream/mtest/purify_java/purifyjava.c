#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
    JNIEnv *env;
    JavaVM *jvm;
    JavaVMInitArgs vm_args;
    JavaVMOption options[2];
    jclass cls;
    jmethodID mid;
    jobjectArray args;
    jstring arg;
    int i;

    if (argc < 2)
      {
	fprintf(stderr, "usage: %s class", argv[0]);
	exit(1);
      }

    options[0].optionString  = (char *) "-Djava.class.path=/vobs/ipms/mas/stream/classes:/vobs/ipms/mas/lib/dom4j-1.6.1.jar:/vobs/ipms/mas/lib/commons-collections-3.1.jar:/vobs/ipms/mas/lib/activation.jar:/vobs/ipms/mas/lib/log4j-1.2.9.jar:/vobs/ipms/mas/lib/junit.jar:/vobs/ipms/mas/lib/jmock-1.0.1.jar:/usr/local/jdk1.5.0_05/jre/lib/rt.jar";
    options[1].optionString = (char *)  "-Djava.compiler=NONE";

    vm_args.version = JNI_VERSION_1_2;
    vm_args.options = options;
    vm_args.nOptions = 2;
    vm_args.ignoreUnrecognized = JNI_FALSE;

    printf("dd1 \n");

    if (JNI_CreateJavaVM(&jvm,(void **)&env,&vm_args) < 0)
    {
	fprintf(stderr, "Can't create JVM\n");
	exit(1);
    }


    printf("dd2 \n");
    cls = (*env)->FindClass(env, argv[1]);
    if (cls == 0) {
        fprintf(stderr, "Couldn't find Java class %s\n", argv[1]);
        exit(1);
    }

    printf("dd3 \n");
    mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
    if (mid == 0) {
      fprintf(stderr, "Couldn't find main in Java class %s\n", argv[1]);
      exit(1);
    }


    printf("dd4 \n");
    args = (*env)->NewObjectArray(env, argc - 2, (*env)->FindClass(env, "java/lang/String"), NULL);
    if (args == 0) {
      fprintf(stderr, "Couldn't create Java string array!\n");
      exit(1);
    }

    printf("dd5 \n");
    for (i = 2; i < argc; ++i)
      {
	arg = (*env)->NewStringUTF(env, argv[i]);
	printf("d 1\n");
	(*env)->SetObjectArrayElement(env, args, i + 2, arg);
	printf("d 2\n");
      }
    
	printf("d 3\n");
    (*env)->CallStaticVoidMethod(env, cls, mid, args);

	printf("d 4\n");
    
    (*jvm)->DestroyJavaVM(jvm);

	printf("d 5\n");

    exit (0);
}
