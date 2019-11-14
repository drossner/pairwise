import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

fun main() {

    System.setProperty("ddl.migration.version", "1.1")
    System.setProperty("ddl.migration.name", "add clicks per concept (spatial)")

    val dbmig = DbMigration.create()
    dbmig.setPlatform(Platform.POSTGRES)

    //dbmig.generateInitMigration()
    dbmig.generateMigration()

}