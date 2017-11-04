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

import com.google.common.base.CaseFormat
import net.pensato.simplicity.extra.idFromEntity
import net.pensato.simplicity.jdbc.exception.NoRecordUpdatedException
import net.pensato.simplicity.extra.toArray
import net.pensato.simplicity.jdbc.mapper.TransactionalRowMapper
import net.pensato.simplicity.jdbc.sql.SqlGenerator
import net.pensato.simplicity.jdbc.sql.SqlGeneratorFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import java.util.*
import org.springframework.data.domain.PageImpl
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


@Repository
abstract class AbstractJdbcRepository<T: Any, ID : Serializable>
@Autowired constructor(var jdbcTemplate: JdbcTemplate, tableName: String, fromClause: String?, jclass: Class<T>, val idName: String) : JdbcRepository<T, ID>, InitializingBean
{
    abstract val rowMapper: TransactionalRowMapper<T>

    val tableDesc: TableDescription
    val sqlGenerator: SqlGenerator

    private var initialized: Boolean = false

    var lastUpdated: LocalDateTime = LocalDateTime.now()
    var counter: Long = -1L

    init {
        val allProperties = jclass.declaredFields
        val columns: Array<String> = Array<String>(allProperties.size - 1, {""})
        var i = 0
        for (p in allProperties) {
            if(p.name != idName)
            columns[i++] = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, p.name)
        }
        val selectClause: String? = if(columns.size > 0) {
                "$idName, ${columns.joinToString(", ")}"
            } else {
                null
            }
        this.tableDesc = TableDescription(tableName, columns, selectClause, fromClause, idName)
        this.sqlGenerator = SqlGeneratorFactory.getGenerator(jdbcTemplate.dataSource)
    }

    constructor(@Autowired jdbcTemplate: JdbcTemplate, tableName: String, jclass: Class<T>, idName: String):
            this(jdbcTemplate, tableName, null, jclass, idName) {}

    constructor(@Autowired jdbcTemplate: JdbcTemplate, jclass: Class<T>, fromClause: String?):
            this(jdbcTemplate, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, jclass.simpleName), fromClause, jclass, "id") {}

    constructor(@Autowired jdbcTemplate: JdbcTemplate, jclass: Class<T>):
            this(jdbcTemplate, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, jclass.simpleName), null, jclass, "id") {}


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
    override fun findAll(pageable: Pageable): Page<T> {
        val list = jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, pageable), rowMapper)
        if (counter == -1L) {
            count()
        }
        return PageImpl<T>(list, pageable, counter)
    }

    @Transactional(readOnly=true)
    override fun findAll(whereClause: String): List<T> {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause), rowMapper)
    }

    @Transactional(readOnly=true)
    override fun findAll(whereClause: String, sort: Sort): List<T> {
        return jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause, sort), rowMapper)
    }

    @Transactional(readOnly=true)
    override fun findAll(whereClause: String, pageable: Pageable): Page<T> {
        val list = jdbcTemplate.query(sqlGenerator.selectAll(tableDesc, whereClause, pageable), rowMapper)
        if (counter == -1L) {
            count()
        }
        return PageImpl<T>(list, pageable, counter)
    }

    @Transactional(readOnly=true)
    override fun findOne(id: Any): T? {
        val resultList =  jdbcTemplate.query(sqlGenerator.selectByPK(tableDesc), arrayOf(id), rowMapper)
        if (resultList != null && resultList.isNotEmpty()) {
            return resultList[0]
        } else {
            return null
        }

    }

    @Transactional(readOnly=true)
    override fun count(): Long {
        val count = jdbcTemplate.queryForObject(sqlGenerator.count(tableDesc), Long::class.java)
        resetCounter(count)
        return count
    }

    @Transactional(readOnly=true)
    override fun exists(id: ID): Boolean {
        return !jdbcTemplate.queryForList(
                sqlGenerator.existsByPK(tableDesc), id.toArray(), Int::class.java).isEmpty()
    }

    @Transactional
    override fun delete(id: Any): Int {
        val lineCount = jdbcTemplate.update(sqlGenerator.deleteByPK(tableDesc), id)
        if (ChronoUnit.MINUTES.between(lastUpdated, LocalDateTime.now()) > 3 || counter == -1L) {
            count()
        } else {
            decreaseCounter()
        }
        return lineCount
    }

    override fun <S : T> save(entity: S): S {
        val id = idFromEntity<T, ID>(entity)
        return if (id == null || id.toString() == "0") {
            val result = create(entity)
            val jclass = entity::class.java
            val properties = jclass.declaredFields
            for (p in properties) {
                if (p.name == idName) {
                    val field = jclass.getDeclaredField(idName)
                    field.setAccessible(true)
                    field.set(entity, result)
                }
            }
            if (ChronoUnit.MINUTES.between(lastUpdated, LocalDateTime.now()) > 3 || counter == -1L) {
                count()
            } else {
                increaseCounter()
            }
            entity
        } else {
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
    override fun <S : T> create(entity: S): ID? {

        val params = rowMapper.columnsValues(entity, tableDesc.columns)
        val id = idFromEntity<T, ID>(entity)

        return if (id == null || id.toString() == "0")
            insertWithAutoGeneratedKey(entity, tableDesc.columns, params)
        else
            insertWithManuallyAssignedKey(entity, tableDesc.columns, params)
    }

    private fun <S : T> insertWithManuallyAssignedKey(entity: S, columns: Array<String>, values: Array<Any>): ID? {
        val insertQuery = sqlGenerator.insert(tableDesc, false)
        jdbcTemplate.update(insertQuery, *values)
        return idFromEntity(entity)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : T> insertWithAutoGeneratedKey(entity: S, columns: Array<String>, values: Array<Any>): ID? {
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
        return keyHolder.key as ID?
    }

    fun increaseCounter() {
        counter += 1
    }
    fun decreaseCounter() {
        counter -= 1
    }
    fun resetCounter(value: Long) {
        counter = value
        lastUpdated = LocalDateTime.now()
    }

}