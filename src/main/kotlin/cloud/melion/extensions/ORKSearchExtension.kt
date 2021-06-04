/* (C)2021 */
package cloud.melion.extensions

import cloud.melion.MySQL
import cloud.melion.annotations.PrimaryKey
import cloud.melion.base.getConstructor
import cloud.melion.interfaces.ITable
import cloud.melion.utils.ObjectMapper
import java.util.*

inline fun <reified T : ITable> search(fields: Map<String, Any>): Optional<List<T>> {
  val table = T::class.java
  assert(table.isAnnotationPresent(PrimaryKey::class.java))
  val primaryKeys = table.getAnnotation(PrimaryKey::class.java).keys
  val constructor = getConstructor(table, primaryKeys)

  if (constructor == null) {
    println("No Constructor found for ${table.simpleName}")
    return Optional.empty()
  }

  println("Saving ${table.simpleName}")
  val sql = StringBuilder("SELECT * FROM ${table.simpleName} ")

  if (fields.isNotEmpty()) {
    sql.append("WHERE ")
  }

  var index = 1
  fields.forEach { (key, value) ->
    sql.append("$key = '$value'")
    if (index < fields.size) {
      index++
      sql.append(" AND ")
    }
  }

  "Output SQL: `$sql`".send()
  val resultSet = MySQL.onQuery(sql.toString()) ?: return Optional.empty()

  val list = ObjectMapper.map<T>(resultSet)
  return Optional.of(list)
}