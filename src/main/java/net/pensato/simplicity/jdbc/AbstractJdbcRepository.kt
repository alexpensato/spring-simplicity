/*
 * Copyright 2017 twitter.com/PensatoAlex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.pensato.simplicity.jdbc

import net.pensato.simplicity.extra.idFromEntity
import net.pensato.simplicity.jdbc.exception.NoRecordUpdatedException
import net.pensato.simplicity.extra.toArray
import net.pensato.simplicity.jdbc.mapper.TransactionalRowMapper
import net.pensato.simplicity.jdbc.sql.SqlGenerator
import net.pensato.simplicity.jdbc.sql.SqlGeneratorFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import java.io.Serializable
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties

@Repository
abstract class AbstractJdbcRepository<T: Any, ID : Serializable>
@Autowired constructor(var jdbcTemplate: JdbcTemplate, tableName: String, klass: KClass<T>, val idName: String) : JdbcRepository<T, ID>, InitializingBean
{
    abstract val rowMapper: TransactionalRowMapper<T>

    val tableDesc: TableDescription
    val sqlGenerator: SqlGenerator

    private var initialized: Boolean = false

    init {
        if (!klass.isData) {
            throw UnknownFormatConversionException("Jdbc repositories support only Kotlin data classes.")
        }
        val allProperties = klass.declaredMemberProperties
        val columns: Array<String> = Array<String>(allProperties.size - 1, {""})
        var i = 0
        for (p in allProperties) {
            if(p.name != idName)
            columns[i++] = p.name
        }
        this.tableDesc = TableDescription(tableName, columns, idName)
        this.sqlGenerator = SqlGeneratorFactory.getGenerator(jdbcTemplate.dataSource)
    }

    override fun afterPropertiesSet() {
        initialized = true
    }

    @Transactional(readOnly=true)
    override fun findAll(): List<T> {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc), rowMapper)
    }

    @Transactional(readOnly=true)
    override fun findAll(ids: Iterable<ID>): List<T> {
        return jdbcTemplate.query(sqlGenerator.selectByPK(tableDesc), rowMapper, *ids.toArray())
    }

    @Transactional(readOnly=true)
    override fun findAll(sort: Sort): List<T> {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, sort), rowMapper)
    }

    @Transactional(readOnly=true)
    override fun findOne(id: Any): T {
        val resultList =  jdbcTemplate.query(sqlGenerator.selectByPK(tableDesc), arrayOf(id), rowMapper)
        Assert.notEmpty(resultList, "")
        return resultList[0]
    }

    @Transactional(readOnly=true)
    override fun count(): Long {
        return jdbcTemplate.queryForObject(sqlGenerator.count(tableDesc), Long::class.java)
    }

    @Transactional(readOnly=true)
    override fun exists(id: ID): Boolean {
        return !jdbcTemplate.queryForList(
                sqlGenerator.existsByPK(tableDesc), id.toArray(), Int::class.java).isEmpty()
    }

    @Transactional
    override fun delete(id: Any): Int {
        return jdbcTemplate.update(sqlGenerator.deleteByPK(tableDesc), id)
    }

    override fun <S : T> save(entity: S): S {
        val id = idFromEntity<T, ID>(entity)
        return if (id == null || id.toString() == "0") {
            val result = insert(entity)
            val properties = entity::class.declaredMemberProperties
            for (p in properties.filterIsInstance<KMutableProperty<*>>()) {
                if (p.name == idName) {
                    p.setter.call(entity, result)
                }
            }
            entity
        } else {
            println(id)
            val rowsAffected = update(entity)
            entity
        }
    }

    override fun <S : T> save(entities: Iterable<S>): List<S> {
        val ret = ArrayList<S>()
        for (s in entities) {
            ret.add(save(s))
        }
        return ret
    }


    @Transactional
    override fun <S : T> update(entity: S): Int {
        val updateQuery = sqlGenerator.update(tableDesc)
        val params = rowMapper.columnsValues(entity, tableDesc.columns)
        val idValue = idFromEntity<T, ID>(entity)
        val rowsAffected = jdbcTemplate.update(updateQuery, *params, idValue)
        if (rowsAffected < 1)
            throw NoRecordUpdatedException(tableDesc.tableName, Array<Any>(1, { idValue.toString() }))
        if (rowsAffected > 1)
            throw JdbcUpdateAffectedIncorrectNumberOfRowsException(updateQuery, 1, rowsAffected)
        return rowsAffected
    }

    @Transactional
    override fun <S : T> insert(entity: S): ID? {

        val params = rowMapper.columnsValues(entity, tableDesc.columns)
        val id = idFromEntity<T, ID>(entity)

        return if (id == null || id.toString() == "0")
            insertWithAutoGeneratedKey(entity, tableDesc.columns, params)
        else
            insertWithManuallyAssignedKey(entity, tableDesc.columns, params)
    }

    open fun <S : T> insertWithManuallyAssignedKey(entity: S, columns: Array<String>, values: Array<Any>): ID? {
        val insertQuery = sqlGenerator.insert(tableDesc, false)
        jdbcTemplate.update(insertQuery, *values)
        return idFromEntity(entity)
    }

    @Suppress("UNCHECKED_CAST")
    open fun <S : T> insertWithAutoGeneratedKey(entity: S, columns: Array<String>, values: Array<Any>): ID? {
        val insertQuery = sqlGenerator.insert(tableDesc, true)
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ con ->
            val idColumnName = tableDesc.pkColumns[0]
            val ps = con.prepareStatement(insertQuery, arrayOf(idColumnName))
            for (i in values.indices) {
                ps.setObject(i + 1, values[i])
            }
            ps
        }, keyHolder)
        return keyHolder.keys.get(idName) as ID?
    }

}