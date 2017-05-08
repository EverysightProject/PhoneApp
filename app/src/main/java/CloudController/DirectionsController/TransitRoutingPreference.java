package CloudController.DirectionsController;



import java.util.Locale;

/**
 * Indicate user preference when requesting transit directions.
 */
public enum TransitRoutingPreference implements StringJoin.UrlValue {
  LESS_WALKING, FEWER_TRANSFERS;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  @Override
  public String toUrlValue() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
