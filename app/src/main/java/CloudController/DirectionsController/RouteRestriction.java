
package CloudController.DirectionsController;


import com.google.maps.internal.StringJoin;

/**
* Directions may be calculated that adhere to certain restrictions. This is configured by calling
* {@link com.google.maps.DirectionsApiRequest#avoid} or {@link com.google.maps.DistanceMatrixApiRequest#avoid}.
*
* @see <a href="https://developers.google.com/maps/documentation/directions/intro#Restrictions">
* Restrictions in the Directions API</a>
* @see <a href="https://developers.google.com/maps/documentation/distancematrix/#Restrictions">
* Restrictions in the Distance Matrix API</a>
*/
public enum RouteRestriction implements StringJoin.UrlValue {

/**
 * {@code TOLLS} indicates that the calculated route should avoid toll roads/bridges.
 */
TOLLS("tolls"),

/**
 * {@code HIGHWAYS} indicates that the calculated route should avoid highways.
 */
HIGHWAYS("highways"),

/**
 * {@code FERRIES} indicates that the calculated route should avoid ferries.
 */
FERRIES("ferries");

private final String restriction;

RouteRestriction(String restriction) {
  this.restriction = restriction;
}

@Override
public String toString() {
  return restriction;
}

@Override
public String toUrlValue() {
  return restriction;
}
}