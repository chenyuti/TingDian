#include "tiny.h"

#include <pthread.h>

extern int clock_gettime(clockid_t __clock_id, struct timespec *__tp);
//extern hash_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;
extern unordered_map<const char *,jstring,hash<const char*>,compare_str> attributeNameCacheMap;

int threadCount;

class statisticsDebugObject
{
	public:
		const char * name;
		double time;
		int count;
		statisticsDebugObject(){
		//	this->name=NULL;this->time=0;count=0;
		}
		statisticsDebugObject(const char * name ,long long time)
		{
			this->name=name;this->time=time;count=1;
		}

		void addNanoTime(long long time){
			this->time+=time;
			count++;
		}
};


typedef unordered_map<const char *,statisticsDebugObject *> statisticsDebugMapType;
static statisticsDebugMapType * statisticsDebugMap;
//0.0004ms
void addStatisticsDebug(const char * name,long long time){
	if(statisticsDebugMap==NULL)
		statisticsDebugMap=new statisticsDebugMapType();

	unordered_map<const char *,statisticsDebugObject *>::iterator it=statisticsDebugMap->find(name);
	if(it==statisticsDebugMap->end()){
		statisticsDebugObject * object=new statisticsDebugObject(name,time);
		statisticsDebugMap->insert(pair<const char *,statisticsDebugObject *>(name, object));
	}
	else{
		statisticsDebugObject * object=it->second;
		object->addNanoTime(time);
	}
}

JNIEXPORT void Java_com_logansoft_UIEngine_parse_XmlParser_showStatisticsDebugInfo(JNIEnv* env,jobject thiz){
	if(statisticsDebugMap==NULL)
		return;
    for(unordered_map<const char *,statisticsDebugObject *>::iterator itb = statisticsDebugMap->begin(); itb!=statisticsDebugMap->end();itb++){
    	statisticsDebugObject * object=itb->second;
        __android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics", "%s  costs:%fms counts:%d  average time:%fms",
        		object->name,object->time/1000000.0,object->count,object->time/object->count/1000000.0);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	jint result = -1;
	jvm=vm;
	if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}
	assert(env != NULL);
	result = JNI_VERSION_1_4;

	StringClass=(jclass)env->NewGlobalRef(env->FindClass("java/lang/String"));
	ObjectClass=(jclass)env->NewGlobalRef(env->FindClass("java/lang/Object"));
	DocumentClass=(jclass)env->NewGlobalRef(env->FindClass("com/logansoft/UIEngine/utils/xml/dom/Document"));
	ElementClass=(jclass)env->NewGlobalRef(env->FindClass("com/logansoft/UIEngine/utils/xml/dom/Element"));
	putAttributeMethodID=env->GetMethodID(ElementClass,"putAttribute","(Ljava/lang/String;Ljava/lang/String;)V");
	putAttributesArrayMethodID=env->GetMethodID(ElementClass,"putAttribute","([Ljava/lang/String;[Ljava/lang/String;)V");
	appendChildMethodID=env->GetMethodID(ElementClass,"appendChild","(Lcom/logansoft/UIEngine/utils/xml/dom/Element;)V");
	newElementMethodID=env->GetMethodID(ElementClass,"<init>","(Ljava/lang/String;II)V");
	newDocumentMethodID=env->GetMethodID(DocumentClass,"<init>","()V");

	return result;
}
pthread_mutex_t mutex;
JNIEXPORT jobject Java_com_logansoft_UIEngine_parse_XmlParser_createXMLWithString( JNIEnv* env,jobject thiz,jobject inStream,int length)
{  
	__android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics","createXMLWithString");

	threadCount=0;
	pthread_mutex_init(&mutex,NULL);

	struct timespec timeStart;
	struct timespec timeEnd;
	clock_gettime(CLOCK_MONOTONIC, &timeStart);
	int cpuCoreCnt = android_getCpuCount();
	__android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics","CPU number:%d",cpuCoreCnt);



	char * str=(char *)malloc((length+1)*sizeof(char));
	jbyteArray BUFFER=env->NewByteArray(4096);
	jclass instreamClass=env->GetObjectClass(inStream);
	jmethodID readMethodID=env->GetMethodID(instreamClass,"read","([B)I");
	int readSize=env->CallIntMethod(inStream,readMethodID,BUFFER);
	char * strp=str;
	while(readSize!=-1){
		env->GetByteArrayRegion(BUFFER,0,readSize,(jbyte *)strp);
		strp+=readSize;
		readSize=env->CallIntMethod(inStream,readMethodID,BUFFER);
	}
	env->DeleteLocalRef(BUFFER);

    TiXmlDocument * document = new TiXmlDocument();
	document->Parse(str);
	free(str);
	clock_gettime(CLOCK_MONOTONIC, &timeEnd);
	long long t=timeEnd.tv_nsec-timeStart.tv_nsec+(timeEnd.tv_sec-timeStart.tv_sec)*1000000000;
	addStatisticsDebug("c++ tinyxml parse",t);

	TiXmlElement* RootElement=document->RootElement();
	


	jobject jdoc=env->NewObject(DocumentClass,newDocumentMethodID);

	jobject jrootElement=parseDomXMLElement(env,RootElement);
	jmethodID setRootElementMethodID=env->GetMethodID(DocumentClass,"setRootElement","(Lcom/logansoft/UIEngine/utils/xml/dom/Element;)V");
	env->CallVoidMethod(jdoc,setRootElementMethodID,jrootElement);
	env->DeleteLocalRef(jrootElement);


	while(true){
		pthread_mutex_lock(&mutex);
		int tc=threadCount;
		pthread_mutex_unlock(&mutex);
		if(tc==0){
			break;
		}
	}
	__android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics","end ,delete Cdocument");

	delete document;


	pthread_mutex_destroy(&mutex);
	return jdoc;
}  
struct asyncArgs{
	TiXmlElement * CElement;
	jobject JElement;
	int threadNumber;
};
void * parseDomXMLElementAsync(asyncArgs * ARGS);
void parseDomXMLElementSync(JNIEnv * env,asyncArgs * ARGS);
jobject parseDomXMLElement(JNIEnv* env,TiXmlElement* CElement)
{
	struct timespec timeStart0;
	struct timespec timeEnd0;

	//clock_gettime(CLOCK_MONOTONIC, &timeStart0);
	TiXmlAttribute * attribute=CElement->FirstAttribute();
	int attributeCount=0,childrenCount=0;
	while(attribute!=NULL){
		attributeCount++;attribute=attribute->Next();
	}
	TiXmlElement * child=CElement->FirstChildElement();
	while(child!=NULL){
		childrenCount++;child=child->NextSiblingElement();
	}
	const char *elementName=CElement->Value();
	jstring ElementName=attributeNameCacheMap[elementName];
	bool needReleaseElementName=false;
	if(ElementName==NULL){
		ElementName= env->NewStringUTF(elementName);
		needReleaseElementName=true;
	}
	jobject jelement=env->NewObject(ElementClass,newElementMethodID,ElementName,attributeCount,childrenCount);
	if(needReleaseElementName)
		env->DeleteLocalRef(ElementName);

	struct asyncArgs * args=(struct asyncArgs *)malloc(sizeof(struct asyncArgs));
	args->CElement=CElement;
	args->JElement=env->NewGlobalRef(jelement);

	pthread_mutex_lock(&mutex);
	if(threadCount<4){
		pthread_t t;
		threadCount++;
		args->threadNumber=threadCount;
		pthread_mutex_unlock(&mutex);
		pthread_create(&t,NULL,(void*(*)(void*))parseDomXMLElementAsync,args);
	}
	else{
		pthread_mutex_unlock(&mutex);
		parseDomXMLElementSync(env,args);
	}

	return jelement;
}
void * parseDomXMLElementAsync(asyncArgs * ARGS)
{
	struct timespec timeStart;
	struct timespec timeEnd;

	clock_gettime(CLOCK_MONOTONIC, &timeStart);
	JNIEnv * env=NULL;
	jvm->AttachCurrentThread(&env,NULL);
	int number=ARGS->threadNumber;
	__android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics","thread start %d",number);
	parseDomXMLElementSync(env,ARGS);
	jvm->DetachCurrentThread();
	pthread_mutex_lock(&mutex);
	threadCount--;
	pthread_mutex_unlock(&mutex);
	pthread_detach(pthread_self());
	__android_log_print(ANDROID_LOG_DEBUG, "LogUtil-jni-Statistics","thread end %d",number);
	clock_gettime(CLOCK_MONOTONIC, &timeEnd);
	long long t=timeEnd.tv_nsec-timeStart.tv_nsec+(timeEnd.tv_sec-timeStart.tv_sec)*1000000000;
		addStatisticsDebug("async parse XML",t);

}
void parseDomXMLElementSync(JNIEnv * env,asyncArgs * ARGS){
	TiXmlElement* CElement=ARGS->CElement;

	TiXmlAttribute * attribute=CElement->FirstAttribute();
	jobject jelement=ARGS->JElement;

	while (attribute!=NULL)
	{
		struct timespec timeStart;
		struct timespec timeEnd;
		const char * name=attribute->Name();
		const char * value=attribute->Value();


		if(value==NULL)value="";

		//clock_gettime(CLOCK_MONOTONIC, &timeStart);
		unordered_map<const char *,jstring>::iterator it=attributeNameCacheMap.find(name);
		jstring Name;
		bool needReleaseName=false;
		if(it==attributeNameCacheMap.end() || it->second==NULL){
			Name=env->NewStringUTF(name);
			attributeNameCacheMap.insert(pair<const char *,jstring>(name, (jstring)env->NewGlobalRef(Name)));
			needReleaseName=true;
		}else{
			Name=it->second;
		}

		it=attributeNameCacheMap.find(value);
		jstring Value;
		bool needReleaseValue=false;
		if(it==attributeNameCacheMap.end() || it->second==NULL){
			Value=env->NewStringUTF(value);
			needReleaseValue=true;
		}else{
			Value=it->second;
		}


	    env->CallVoidMethod(jelement,putAttributeMethodID,Name,Value);

		if(needReleaseName)
			env->DeleteLocalRef(Name);
	    if(needReleaseValue)
	    	env->DeleteLocalRef(Value);
		attribute=attribute->Next();
	}

	TiXmlElement * child=CElement->FirstChildElement();
	while(child!=NULL)
	{
		jobject jchild=parseDomXMLElement(env,child);
		env->CallVoidMethod(jelement,appendChildMethodID,jchild);

		child=child->NextSiblingElement();
		env->DeleteLocalRef(jchild);
	}	

	env->DeleteGlobalRef(ARGS->JElement);

	delete ARGS;
}

