package mate.jdbc;

import java.util.Optional;
import mate.jdbc.dao.Dao;
import mate.jdbc.dao.ManufacturerDao;
import mate.jdbc.lib.Injector;
import mate.jdbc.model.Manufacturer;

public class Main {

    private static final Injector injector = Injector.getInstance("mate.jdbc.dao");

    public static void main(String[] args) {
        ManufacturerDao dao = (ManufacturerDao) injector.getInstance(Dao.class);

        // create
        Manufacturer ford = dao.create(new Manufacturer("Ford", "USA"));
        System.out.printf("---> %s was added to DB%n", ford);

        // create
        Manufacturer bmw = dao.create(new Manufacturer("BMW", "Germany"));
        System.out.printf("---> %s was added to DB%n%n", bmw);

        // get
        Optional<Manufacturer> fordFromDb = dao.get(ford.getId());
        fordFromDb.ifPresent(el ->
                System.out.printf("---> Get by id from DB: %s%n", el));
        // get
        Optional<Manufacturer> bmwFromDb = dao.get(ford.getId());
        fordFromDb.ifPresent(el ->
                System.out.printf("---> Get by id from DB: %s%n%n", bmwFromDb));

        // update
        ford.setName("Updated Ford");
        ford.setCountry("UK");
        Manufacturer updatedFord = dao.update(ford);
        Manufacturer previousFord = fordFromDb.orElse(null);
        System.out.printf("---> %s was updated to %s%n", previousFord, updatedFord);

        // get
        Optional<Manufacturer> updatedFordFromDb = dao.get(ford.getId());
        updatedFordFromDb.ifPresent(el ->
                System.out.printf("---> Get updated manufacturer from DB: %s%n%n", el));

        // get all from db
        System.out.printf("---> Get all from DB: %s%n", dao.getAll());

        // delete
        if (dao.delete(updatedFord.getId())) {
            System.out.printf("---> %s was deleted from DB%n", updatedFord);
        }

        // get all from db
        System.out.printf("---> Get all from DB: %s%n", dao.getAll());
    }
}
