
# RouteParameters

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**origin** | **Object** | origin by GEO |  [optional]
**destination** | **Object** | destination by GEO |  [optional]
**travelMode** | [**TravelModeEnum**](#TravelModeEnum) | Choose how to travel - Driving,Walking,Bicycling,transit,unknown |  [optional]
**avoid** | [**AvoidEnum**](#AvoidEnum) | Avoid options - Tolls,Highways,Ferries |  [optional]
**units** | [**UnitsEnum**](#UnitsEnum) | Unit system - Metric,Imperial |  [optional]
**region** | **String** | The region code, specified as a ccTLD |  [optional]
**arrivalTime** | [**BigDecimal**](BigDecimal.md) | time to arrive in millis sence epoc |  [optional]
**departureTime** | [**BigDecimal**](BigDecimal.md) | time to depart in millis sence epoc |  [optional]
**places** | **List&lt;String&gt;** | Places to include in the route |  [optional]
**waypoints** | [**List&lt;GeoLocation&gt;**](GeoLocation.md) | Waypoints to include in the route in GEO |  [optional]
**alternatives** | **Boolean** | Whether retuning one route or multiple choices |  [optional]
**transmitMode** | [**TransmitModeEnum**](#TransmitModeEnum) | Transmit mode - Bus,Subway,Train,Tram |  [optional]
**transitRoutingPreference** | [**TransitRoutingPreferenceEnum**](#TransitRoutingPreferenceEnum) | Rounting prefernces - Less walking, fewer transfers |  [optional]
**traficModel** | [**TraficModelEnum**](#TraficModelEnum) | How to calculate times - best guess, Optimistic, Pasimistic |  [optional]


<a name="TravelModeEnum"></a>
## Enum: TravelModeEnum
Name | Value
---- | -----


<a name="AvoidEnum"></a>
## Enum: AvoidEnum
Name | Value
---- | -----


<a name="UnitsEnum"></a>
## Enum: UnitsEnum
Name | Value
---- | -----


<a name="TransmitModeEnum"></a>
## Enum: TransmitModeEnum
Name | Value
---- | -----


<a name="TransitRoutingPreferenceEnum"></a>
## Enum: TransitRoutingPreferenceEnum
Name | Value
---- | -----


<a name="TraficModelEnum"></a>
## Enum: TraficModelEnum
Name | Value
---- | -----



