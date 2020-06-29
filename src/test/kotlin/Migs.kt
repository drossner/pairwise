import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

fun main() {

    //if you create a new initial sql and xml file use version .0 and change name to "initial"
    System.setProperty("ddl.migration.version", "2.0")
    System.setProperty("ddl.migration.name", "init")

    val dbmig = DbMigration.create()
    dbmig.setPlatform(Platform.POSTGRES)

    //delete the other xml in model and the sql files in dbmigration and use this code to create new initial sql and xml file
    //dbmig.generateInitMigration()
    //use this code to add new tables or columns to tables
    dbmig.generateMigration()

}