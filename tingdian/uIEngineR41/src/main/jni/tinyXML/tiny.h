#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>

#include "tinyxml.h"
#include "tinystr.h"

//#include <hash_map>
#include <time.h>
//#include <map>
#include <assert.h>
#include <android/log.h>
#include <cpu-features.h>

#include <unordered_map>

using namespace std;
using namespace std::tr1;

static JavaVM * jvm;

static jclass DocumentClass;
static jclass ElementClass;
static jclass StringClass;
static jclass ObjectClass;
static jmethodID newElementMethodID;
static jmethodID newDocumentMethodID;
static jmethodID putAttributeMethodID;
static jmethodID putAttributesArrayMethodID;
static jmethodID appendChildMethodID;
static jmethodID addItemNameandTimeNano;

//extern int threadCount;
//static pthread_t thread[4];


struct compare_str{
	bool operator()(const char* p1, const char*p2) const{
		return strcmp(p1,p2)==0;
	}
};
//extern hash_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;
extern unordered_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;

extern "C"{
	JNIEXPORT void Java_com_logansoft_UIEngine_parse_XmlParser_registeAttributeNameString(JNIEnv* env,jobject thiz,jobjectArray registerStrings);
}
extern "C"{
	JNIEXPORT jobject Java_com_logansoft_UIEngine_parse_XmlParser_createXMLWithString( JNIEnv* env,jobject thiz,jobject inStream,int length);
}
extern "C"{
	JNIEXPORT void Java_com_logansoft_UIEngine_parse_XmlParser_showStatisticsDebugInfo(JNIEnv* env,jobject thiz);
}
inline long long gettime(struct timespec * timeStart,struct timespec * timeEnd){
	return timeEnd->tv_nsec-timeStart->tv_nsec+(timeEnd->tv_sec-timeStart->tv_sec)*1000000000;
}
jobject parseDomXMLElement(JNIEnv* env,TiXmlElement* CElement);
