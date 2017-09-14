package net.pensato.simplicity.web

import net.pensato.simplicity.extra.idFromEntity
import net.pensato.simplicity.jdbc.JdbcRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.*
import java.io.Serializable

abstract class AbstractController<T: Any, ID : Serializable>
@Autowired constructor(var repository: JdbcRepository<T, ID>)
{
    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun findAll(pageable: Pageable): Page<T> {
        return repository.findAll(pageable)
    }

    @RequestMapping(value = "/count", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun count(): Long {
        return repository.count()
    }

    @RequestMapping(value = "/{id}", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findById(@PathVariable id: Long?): T? {
        Assert.notNull(id, "You must provide an ID to locate an item in the repository.")
        return repository.findOne(id!!)
    }

    @RequestMapping(value = "/{id}", method = arrayOf(RequestMethod.DELETE))
    @ResponseBody
    fun delete(@PathVariable id: Long?): String {
        Assert.notNull(id, "You must provide an ID to delete an item from the repository.")
        return "Total itens deleted: ${repository.delete(id!!)}."
    }

    @RequestMapping(value = "/{id}", method = arrayOf(RequestMethod.PUT))
    @ResponseBody
    fun update(@PathVariable id: Long?, @RequestBody t: T): String {
        Assert.notNull(id, "You must provide an ID to update an item in the repository.")
        val entityId = idFromEntity<T, ID>(t)
        Assert.state(entityId == id, "The item you are trying to update is not the same as the pointed repository location.")
        return "Total itens updated: ${repository.update(t)}."
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.CREATED)
    fun insert(@RequestBody t: T): T {
        return repository.save(t)
    }

}
