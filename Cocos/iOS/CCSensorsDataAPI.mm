//
// CCSensorsDataAPI.m
// SensorsData
//
// Created by yuqiang on 2021/10/14.
// Copyright © 2021 Sensors Data Co., Ltd. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#if ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Either turn on ARC for the project or use -fobjc-arc flag on this file.
#endif

#import "CCSensorsDataAPI.h"
#if __has_include(<SensorsAnalyticsSDK/SensorsAnalyticsSDK.h>)
#import <SensorsAnalyticsSDK/SensorsAnalyticsSDK.h>
#else
#import "SensorsAnalyticsSDK.h"
#endif

NSString * const SACocosCreatorPluginVersionKey = @"$lib_plugin_version";
NSString * const SACocosCreatorPluginVersionValue = @"app_cocos_creator:0.0.1";

@interface CCSensorsDataUtil : NSObject

@end

@implementation CCSensorsDataUtil

/// 通过插件触发的事件, 添加 $lib_plugin_version 属性
/// 1. 在应用程序生命周期中, 第一次通过插件 track 事件时, 需要添加 $lib_plugin_version 属性, 后续事件无需添加该属性
/// 2. 当用户的属性中包含 $lib_plugin_version 时, 插件不进行覆盖
/// @param properties 事件属性
+ (NSDictionary *)appendPluginVersion:(NSDictionary *)properties {
    if (properties[SACocosCreatorPluginVersionKey]) return properties;
    __block NSMutableDictionary *result;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        result = [NSMutableDictionary dictionaryWithDictionary:properties];
        result[SACocosCreatorPluginVersionKey] = @[SACocosCreatorPluginVersionValue];
    });
    return result ? [result copy] : properties;
}

+ (NSDictionary *)jsonObjectWithString:(NSString *)string {
    if (![string isKindOfClass:NSString.class]) {
        return nil;
    }

    NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
    if (!data) {
        return nil;
    }

    id jsonObject = nil;
    @try {
        NSError *jsonError = nil;
        jsonObject = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&jsonError];
    } @catch (NSException *exception) {
    }

    if ([jsonObject isKindOfClass:NSDictionary.class]) {
        return (NSDictionary *)jsonObject;
    }

    return nil;
}

+ (NSString *)jsonStringWithObject:(NSDictionary *)object {
    if (!object) {
        return nil;
    }

    if (![NSJSONSerialization isValidJSONObject:object]) {
        return nil;
    }

    NSData *data = nil;
    @try {
        NSError *error = nil;
        data = [NSJSONSerialization dataWithJSONObject:object options:0 error:&error];
    } @catch (NSException *exception) {
    }
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

@end

@implementation CCSensorsDataAPI

+ (void)initSA:(NSString *)json {
    NSDictionary *dic = [CCSensorsDataUtil jsonObjectWithString:json];
    if (!dic) {
        return;
    }
    // 原生 SDK 未对 url 做类型校验, 类型错误会导致原生 SDK Crash
    id server = dic[@"serverUrl"];
    NSString *serverURL = [server isKindOfClass:NSString.class] ? server : @"";
    BOOL appStart = [dic[@"appStart"] boolValue];
    BOOL appEnd = [dic[@"appEnd"] boolValue];
    BOOL enableLog = [dic[@"enableLog"] boolValue];
    BOOL enableEncrypt = [dic[@"enableEncrypt"] boolValue];

    SAConfigOptions *config = [[SAConfigOptions alloc] initWithServerURL:serverURL launchOptions:nil];
    config.enableLog = enableLog;
    config.enableEncrypt = enableEncrypt;
    SensorsAnalyticsAutoTrackEventType autoTrackType = SensorsAnalyticsEventTypeNone;
    if (appStart) {
        autoTrackType = autoTrackType | SensorsAnalyticsEventTypeAppStart;
    }
    if (appEnd) {
        autoTrackType = autoTrackType | SensorsAnalyticsEventTypeAppEnd;
    }
    config.autoTrackEventType = autoTrackType;
    [SensorsAnalyticsSDK startWithConfigOptions:config];
}

+ (void)track:(NSString *)event properties:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    NSDictionary *result = [CCSensorsDataUtil appendPluginVersion:properties];
    [SensorsAnalyticsSDK.sharedInstance track:event withProperties:result];
}

+ (void)login:(NSString *)distinctID {
    NSDictionary *properties = [CCSensorsDataUtil appendPluginVersion:nil];
    [SensorsAnalyticsSDK.sharedInstance login:distinctID withProperties:properties];
}

+ (void)logout {
    [SensorsAnalyticsSDK.sharedInstance logout];
}

+ (void)identify:(NSString *)distinctID {
    [SensorsAnalyticsSDK.sharedInstance identify:distinctID];
}

+ (void)setOnceProfile:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    [SensorsAnalyticsSDK.sharedInstance setOnce:properties];
}

+ (void)setProfile:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    [SensorsAnalyticsSDK.sharedInstance set:properties];
}

+ (void)registerSuperProperties:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    [SensorsAnalyticsSDK.sharedInstance registerSuperProperties:properties];
}

+ (void)unregisterSuperProperty:(NSString *)superProperty {
    [SensorsAnalyticsSDK.sharedInstance unregisterSuperProperty:superProperty];
}

+ (void)increment:(NSString *)key by:(NSNumber *)number {
    [SensorsAnalyticsSDK.sharedInstance increment:key by:number];
}

+ (void)profileAppend:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    NSString *key = properties.allKeys.firstObject;
    NSArray *value = properties.allValues.firstObject;
    [SensorsAnalyticsSDK.sharedInstance append:key by:value];
}

+ (void)profileUnset:(NSString *)profile {
    [SensorsAnalyticsSDK.sharedInstance unset:profile];
}

+ (void)profileDelete {
    [SensorsAnalyticsSDK.sharedInstance deleteUser];
}

+ (void)flush {
    [SensorsAnalyticsSDK.sharedInstance flush];
}

+ (NSString *)trackTimerStart:(NSString *)event {
    return [SensorsAnalyticsSDK.sharedInstance trackTimerStart:event];
}

+ (void)trackTimerPause:(NSString *)event {
    [SensorsAnalyticsSDK.sharedInstance trackTimerPause:event];
}

+ (void)trackTimerResume:(NSString *)event {
    [SensorsAnalyticsSDK.sharedInstance trackTimerResume:event];
}

+ (void)trackTimerEnd:(NSString *)event withProperties:(NSString *)json {
    if (![event isKindOfClass:[NSString class]]) {
        return;
    }
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    NSDictionary *result = [CCSensorsDataUtil appendPluginVersion:properties];
    [SensorsAnalyticsSDK.sharedInstance trackTimerEnd:event withProperties:result];
}

+ (void)trackAppInstall:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    [SensorsAnalyticsSDK.sharedInstance trackAppInstallWithProperties:properties];
}

+ (void)trackAppViewScreen:(NSString *)url withProperties:(NSString *)json {
    NSDictionary *properties = [CCSensorsDataUtil jsonObjectWithString:json];
    NSDictionary *result = [CCSensorsDataUtil appendPluginVersion:properties];
    [SensorsAnalyticsSDK.sharedInstance trackViewScreen:url withProperties:result];
}

+ (NSString *)getDistinctId {
    return [SensorsAnalyticsSDK.sharedInstance distinctId];
}

+ (NSString *)getAnonymousId {
    return [SensorsAnalyticsSDK.sharedInstance anonymousId];
}

+ (NSString *)getLoginId {
    return [SensorsAnalyticsSDK.sharedInstance loginId];
}

+ (NSString *)getPresetProperties {
    NSDictionary *properties = [SensorsAnalyticsSDK.sharedInstance getPresetProperties];
    return [CCSensorsDataUtil jsonStringWithObject:properties];
}

@end
