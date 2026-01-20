from dataclasses import dataclass


@dataclass(frozen=True)
class VehicleDetails:
    """Represents vehicle details including VIN, dealer code, zipcode, and vehicle type."""

    vin: str
    dealercode: str
    zipcode: str
    vehicletype: str

    def __str__(self) -> str:
        """Return a JSON-like string representation of the vehicle details.

        Returns:
            str: JSON-formatted string of vehicle details
        """
        return f'{{"vin": "{self.vin}", "dealercode": "{self.dealercode}", ' \
               f'"zipcode": "{self.zipcode}", "vehicletype": "{self.vehicletype}"}}'

    def __eq__(self, other: object) -> bool:
        """Compare two VehicleDetails objects for equality.

        Args:
            other: Object to compare with

        Returns:
            bool: True if objects are equal, False otherwise
        """
        if not isinstance(other, VehicleDetails):
            return False
        return (self.vin == other.vin and
                self.dealercode == other.dealercode and
                self.zipcode == other.zipcode)

    def __hash__(self) -> int:
        """Generate a hash value for the object.

        Returns:
            int: Hash value
        """
        return hash((self.vin, self.dealercode, self.zipcode))
