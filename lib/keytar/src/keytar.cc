/**
 * ref : https://github.com/davidafsilva/jkeychain/tree/master/src/main/c
 */
#include "com_starxg_keytar_Keytar.h"
#include "keytar.h"
#include <string>
#include <vector>

#define KeytarException "com/starxg/keytar/KeytarException"

/* Throw an exception.
 *
 * Parameters:
 *	env				The JNI environment.
 *	exceptionClass	The name of the exception class.
 *	message			The message to pass to the Exception.
 */
void throw_exception(JNIEnv *env, const char *exceptionClass, const char *message)
{
    jclass cls = env->FindClass(exceptionClass);
    /* if cls is NULL, an exception has already been thrown */
    if (cls != nullptr)
    {
        env->ThrowNew(cls, message);
    }
    /* free the local ref, utility funcs must delete local refs. */
    env->DeleteLocalRef(cls);
}

/* A simplified structure for dealing with jstring objects. Use jstring_unpack
 * and jstring_unpacked_free to manage these.
 */
typedef struct
{
    int len;
    const char *str;
} jstring_unpacked;

/* Unpack the data from a jstring and put it in a jstring_unpacked.
 *
 * Parameters:
 *	env	The JNI environment.
 *	js	The jstring to unpack.
 *	ret	The jstring_unpacked in which to store the result.
 */
void jstring_unpack(JNIEnv *env, jstring js, jstring_unpacked *ret)
{
    if (ret == nullptr)
    {
        return;
    }
    if (env == nullptr || js == nullptr)
    {
        ret->len = 0;
        ret->str = nullptr;
        return;
    }

    /* Get the length of the string. */
    ret->len = (int)(env->GetStringUTFLength(js));
    if (ret->len <= 0)
    {
        ret->len = 0;
        ret->str = nullptr;
        return;
    }
    ret->str = env->GetStringUTFChars(js, nullptr);
}

/* Clean up a jstring_unpacked after it's no longer needed.
 *
 * Parameters:
 *	jsu	A jstring_unpacked structure to clean up.
 */
void jstring_unpacked_free(JNIEnv *env, jstring js, jstring_unpacked *jsu)
{
    if (jsu != nullptr && jsu->str != nullptr)
    {
        env->ReleaseStringUTFChars(js, jsu->str);
        jsu->len = 0;
        jsu->str = nullptr;
    }
}

JNIEXPORT jstring JNICALL Java_com_starxg_keytar_Keytar__1getPassword(JNIEnv *env, jobject, jstring serviceName, jstring accountName)
{
    jstring_unpacked service_name;
    jstring_unpacked account_name;
    jstring result = nullptr;

    jstring_unpack(env, serviceName, &service_name);
    jstring_unpack(env, accountName, &account_name);

    std::string password;
    std::string error;

    keytar::KEYTAR_OP_RESULT ret = keytar::GetPassword(service_name.str, account_name.str, &password, &error);

    if (ret == keytar::FAIL_ERROR)
    {
        throw_exception(env, KeytarException, error.c_str());
    }

    jstring_unpacked_free(env, serviceName, &service_name);
    jstring_unpacked_free(env, accountName, &account_name);

    if (ret == keytar::FAIL_NONFATAL)
    {
        return nullptr;
    }

    return env->NewStringUTF(password.c_str());
}

JNIEXPORT void JNICALL Java_com_starxg_keytar_Keytar__1setPassword(JNIEnv *env, jobject, jstring serviceName, jstring accountName, jstring password)
{
    jstring_unpacked service_name;
    jstring_unpacked account_name;
    jstring_unpacked service_password;

    /* Unpack the params */
    jstring_unpack(env, serviceName, &service_name);
    jstring_unpack(env, accountName, &account_name);
    jstring_unpack(env, password, &service_password);

    std::string error;

    keytar::KEYTAR_OP_RESULT ret = keytar::SetPassword(service_name.str, account_name.str, service_password.str, &error);
    if (ret != keytar::SUCCESS)
    {
        throw_exception(env, KeytarException, error.c_str());
    }

    /* Clean up. */
    jstring_unpacked_free(env, serviceName, &service_name);
    jstring_unpacked_free(env, accountName, &account_name);
    jstring_unpacked_free(env, password, &service_password);
}

/*
 * Class:     com_starxg_keytar_Keytar
 * Method:    deletePassword
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_starxg_keytar_Keytar__1deletePassword(JNIEnv *env, jobject, jstring serviceName, jstring accountName)
{
    jstring_unpacked service_name;
    jstring_unpacked account_name;

    /* Unpack the params. */
    jstring_unpack(env, serviceName, &service_name);
    jstring_unpack(env, accountName, &account_name);

    std::string error;

    keytar::KEYTAR_OP_RESULT ret = keytar::DeletePassword(service_name.str, account_name.str, &error);
    if (ret == keytar::FAIL_ERROR)
    {
        throw_exception(env, KeytarException, error.c_str());
    }

    jstring_unpacked_free(env, serviceName, &service_name);
    jstring_unpacked_free(env, accountName, &account_name);

    return ret != keytar::FAIL_ERROR;
}

/*
 * Class:     com_starxg_keytar_Keytar
 * Method:    getCredentials
 * Signature: (Ljava/lang/String;)Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_com_starxg_keytar_Keytar__1getCredentials(JNIEnv *env, jobject, jstring serviceName)
{

    jstring_unpacked service_name;

    jstring_unpack(env, serviceName, &service_name);

    std::string error;
    std::vector<keytar::Credentials> credentials;
    jobject map = nullptr;

    keytar::KEYTAR_OP_RESULT ret = keytar::FindCredentials(service_name.str, &credentials, &error);

    if (ret == keytar::FAIL_ERROR)
    {
        throw_exception(env, KeytarException, error.c_str());
    }
    else if (ret == keytar::SUCCESS)
    {

        jclass clazz = env->FindClass("java/util/HashMap");
        jmethodID init = env->GetMethodID(clazz, "<init>", "(I)V");
        jmethodID put = env->GetMethodID(clazz, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        map = env->NewObject(clazz, init, 10);

        for (int i = 0; i < credentials.size(); i++)
        {
            keytar::Credentials cred = credentials[i];
            jobject account = env->NewStringUTF(cred.first.c_str());
            jobject password = env->NewStringUTF(cred.second.c_str());
            env->CallObjectMethod(map, put, account, password);
        }
    }

    jstring_unpacked_free(env, serviceName, &service_name);

    return map;
}