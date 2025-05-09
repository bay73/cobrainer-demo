package cobrainer.demo.repository

import com.fasterxml.jackson.annotation.JsonValue
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

interface WrappedUuid<T> : Comparable<WrappedUuid<T>> {
    @get:JsonValue
    val asUuid: UUID

    /**
     * https://bugs.openjdk.org/browse/JDK-7025832 - compareTo on UUID is implemented wrong.
     *
     * it doesn't sort lexicographically (treating UUID as a hex String) and also not numerically
     * (if treated as one large number or a byte array), instead the implementation has 2 signed long overflow
     * issues (with first and 17th character), resulting in very strange sorting order.
     *
     * E.g.:
     * 7xxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx > 5xxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx == true
     * 9xxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx > 5xxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx == false (overflow error)
     * and
     * 00000000-0000-4000-7xxx-xxxxxxxxxxxx > 00000000-0000-4000-5xxx-xxxxxxxxxxxx == true
     * 00000000-0000-4000-9xxx-xxxxxxxxxxxx > 00000000-0000-4000-5xxx-xxxxxxxxxxxx == false (overflow error)
     *
     * while any other location is handled correct, e.g.
     * 07xxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx > 05xxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx == true
     * 09xxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx > 05xxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx == true
     * and
     * 00000000-0000-4000-07xx-xxxxxxxxxxxx > 00000000-0000-4000-05xx-xxxxxxxxxxxx == true
     * 00000000-0000-4000-09xx-xxxxxxxxxxxx > 00000000-0000-4000-05xx-xxxxxxxxxxxx == true
     *
     *
     * This implementation fixes the issue, to make sorting consistent with other programming languages and
     * the POSTGRES DB.
     */
    override fun compareTo(other: WrappedUuid<T>): Int {
        val most = asUuid.mostSignificantBits.toULong().compareTo(other.asUuid.mostSignificantBits.toULong())

        return if (most == 0) {
            asUuid.leastSignificantBits.toULong().compareTo(other.asUuid.leastSignificantBits.toULong())
        } else {
            most
        }
    }
}

/**
 * A base class to represent all custom column types that just wrap UUID values, as e.g. UserId does.
 *
 * The logic is always identical, so it's enough to just extend the class without overriding anything.
 */
class WrappedUuidColumnType<T : WrappedUuid<T>>(
    private val klass: KClass<T>,
) : ColumnType<T>() {
    private val constr: KFunction<T> = klass.primaryConstructor!!

    override fun sqlType() = currentDialect.dataTypeProvider.uuidType()

    // The method name is misleading. The [Any] attribute can be either
    // already the wanted type [T], or the database type [UUID] here
    override fun valueFromDB(value: Any): T =
        when {
            value is UUID -> constr.call(value)
            (value.javaClass == klass.java) ->
                @Suppress("UNCHECKED_CAST")
                (value as T)
            else -> error("$value of ${value.javaClass} is not valid for ${klass.java}")
        }

    /**
     * Used to map values from specific id class to generic UUID class
     */
    override fun notNullValueToDB(value: T) = value.asUuid

    override fun nonNullValueToString(value: T): String = "'${notNullValueToDB(value)}'"
}

/**
 * Declare a column as type UUID but with implicit mapping to specific UUID wrapper class
 */
inline fun <reified T : WrappedUuid<T>> Table.uuid(
    name: String,
    type: KClass<T>,
): Column<T> = registerColumn(name, WrappedUuidColumnType(type))
