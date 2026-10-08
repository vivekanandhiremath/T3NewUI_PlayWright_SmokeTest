package utilities;

import java.util.Objects;

public class VehicleDetails {

    private final String vin;
    private final String dealercode;
    private final String zipcode;
    private final String vehicletype;

    public VehicleDetails(String vin, String dealercode, String zipcode, String vehicletype) {
        this.vin = vin;
        this.dealercode = dealercode;
        this.zipcode = zipcode;
        this.vehicletype = vehicletype;
    }

    public String getVin() {
        return vin;
    }

    public String getDealercode() {
        return dealercode;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getVehicletype() {
        return vehicletype;
    }

    @Override
    public String toString() {
        return "{\"vin\": \"" + vin + "\", \"dealercode\": \"" + dealercode + "\", " +
                "\"zipcode\": \"" + zipcode + "\", \"vehicletype\": \"" + vehicletype + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleDetails that = (VehicleDetails) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(dealercode, that.dealercode) &&
                Objects.equals(zipcode, that.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, dealercode, zipcode);
    }
}
