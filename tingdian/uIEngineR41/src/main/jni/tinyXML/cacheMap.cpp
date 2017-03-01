#include "tiny.h"

//hash_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;
unordered_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;


JNIEXPORT void Java_com_logansoft_UIEngine_parse_XmlParser_registeAttributeNameString(JNIEnv* env,jobject thiz,jobjectArray registerStrings){

	int length=env->GetArrayLength(registerStrings);
	for(int i=0;i<length;i++){
		jstring jstr=(jstring)env->GetObjectArrayElement(registerStrings,i);
		const char* cstring = env->GetStringUTFChars(jstr,NULL);
		if(attributeNameCacheMap[cstring]==NULL)
			attributeNameCacheMap[cstring]=(jstring)env->NewGlobalRef(jstr);
		//env->ReleaseStringUTFChars(jstr,cstring);
	}

}
