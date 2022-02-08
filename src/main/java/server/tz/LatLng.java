package server.tz;

import java.io.Serializable;

public class LatLng implements Serializable { /** * Количество результатов */

    public int resultsCount;
    String pos;
    double lat;
    double lng; /** * Construct a LatLng * * @param pos a String in format "lng, lat", e.g. "45.545345, 64.23423" */

    public LatLng(String pos) {
        String[] ll = pos.split(" ");
        if (ll.length != 2) {
            throw new IllegalArgumentException( "Format of pos in not supported. Should be \"lat, lng\", e.g. \"45.545345, 64.23423\"");
        }
        lat = Double.parseDouble(ll[1]);
        lng = Double.parseDouble(ll[0]);
        this.pos = pos;
    } /** * Construct a LatLng * * @param lat latitude * @param lng longitude */

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.pos = String.format("%f %f", lat, lng);
    } /** * @return latitude */

    public double getLat() {
        return lat;
    } /** * set latitude */

    public void setLat(double lat) {
        this.lat = lat;
    } /** * @return longitude */

    public double getLng() {
        return lng;
    } /** * set longitude */

    public void setLng(double lng) {
        this.lng = lng;
    } /** * {@inheritDoc} */

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true; }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LatLng that = (LatLng) o;
        if (Double.compare(lat, that.lat) != 0) {
            return false;
        } return Double.compare(lng, that.lng) == 0;
    } /** * {@inheritDoc} */

    @Override public int hashCode() {
        int result = hashDouble(lat);
        return 31 * result + hashDouble(lng);
    }/** * A tiny hashcode for double snitched from Double sources * * @see java.lang.Double */

    private int hashDouble(double value) {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    } /** * Получить {@link #resultsCount} * * @return {@link #resultsCount} */

    public int getResultsCount() {
        return resultsCount;
    } /** * Установить {@link #resultsCount} * * @param resultsCount {@link #resultsCount} */

    public void setResultsCount(int resultsCount) {
        this.resultsCount = resultsCount;
    }
}