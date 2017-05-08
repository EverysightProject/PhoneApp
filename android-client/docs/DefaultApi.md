# DefaultApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**directionsGet**](DefaultApi.md#directionsGet) | **GET** /directions | 


<a name="directionsGet"></a>
# **directionsGet**
> directionsGet(origin, destination, routeparameters)



Get directions between two locations 

### Example
```java
// Import classes:
//import io.swagger.client.api.DefaultApi;

DefaultApi apiInstance = new DefaultApi();
String origin = "origin_example"; // String | origin locations by description
String destination = "destination_example"; // String | destination location by description
RouteParameters routeparameters = new RouteParameters(); // RouteParameters | 
try {
    apiInstance.directionsGet(origin, destination, routeparameters);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#directionsGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **origin** | **String**| origin locations by description | [optional]
 **destination** | **String**| destination location by description | [optional]
 **routeparameters** | [**RouteParameters**](RouteParameters.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

