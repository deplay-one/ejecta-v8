//
// Created by Martin Kleinhans on 24.08.17.
//

#include "JNIV8Wrapper.h"
#include "JNIV8GenericObject.h"

BGJS_JNIV8OBJECT_LINK(JNIV8GenericObject, "ag/boersego/bgjs/JNIV8GenericObject");

void JNIV8GenericObject::initializeJNIBindings(JNIClassInfo *info, bool isReload) {
    info->registerNativeMethod("NewInstance", "(J)Lag/boersego/bgjs/JNIV8GenericObject;", (void*)JNIV8GenericObject::NewInstance);
}

void JNIV8GenericObject::initializeV8Bindings(V8ClassInfo *info) {

}

jobject JNIV8GenericObject::NewInstance(JNIEnv *env, jobject obj, jlong enginePtr) {
    BGJSV8Engine *engine = reinterpret_cast<BGJSV8Engine*>(enginePtr);

    v8::Isolate* isolate = engine->getIsolate();
    v8::Locker l(isolate);
    v8::Isolate::Scope isolateScope(isolate);
    v8::HandleScope scope(isolate);
    v8::Context::Scope ctxScope(engine->getContext());

    v8::Local<v8::Object> objRef = v8::Object::New(isolate);

    return JNIV8Wrapper::wrapObject<JNIV8GenericObject>(objRef)->getJObject();
}