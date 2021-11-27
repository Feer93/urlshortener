package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.*
import org.springframework.data.domain.Sort
import java.net.URI

/**
 * Implementation of the port [ClickRepositoryService].
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    override fun save(cl: Click): Click = clickEntityRepository.save(cl.toEntity()).toDomain()
}

/**
 * Implementation of the port [ShortUrlRepositoryService].
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    override fun findByKey(id: String): ShortUrl? = shortUrlEntityRepository.findByHash(id)?.toDomain()

    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()
}

class InfoRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository,
    private val clickEntityRepository: ClickEntityRepository
) : InfoRepositoryService {

    private fun simulateSlowService() {
        try {
            val time = 3000L
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            throw IllegalStateException(e)
        }
    }

    override fun countURL(): Long = shortUrlEntityRepository.count()
    override fun countRedirection(): Long = clickEntityRepository.count()

    override fun recoverTopKShortenedURL(k: Int): MutableList<Pair<String, Long>>{
        simulateSlowService()
        val out: MutableList<Pair<String, Long>> = mutableListOf()

        val aux: HashMap<String, Long> = HashMap()
        val list  = shortUrlEntityRepository.findAll()
        for(entity in list) {
            val host = URI(entity.toDomain().redirection.target).host
            if(host != null){
                if(aux.containsKey(host)){
                    aux[host] = aux.getValue(host) + 1
                } else{
                    aux[host] = 1
                }
            }
        }
        for(key in aux.keys){
            out.add(Pair(key, aux.getValue(key)))
        }
        out.sortByDescending { it.second }
        return if(k < out.size){
            out.subList(0, k-1)
        } else {
            out
        }

    }

    override fun recoverTopKRedirection(k: Int): MutableList<Pair<String, Long>> {
        val out: MutableList<Pair<String, Long>> = mutableListOf()
        val aux: HashMap<String, Long> = HashMap()
        val list  = clickEntityRepository.findAll()

        for(entity in list) {
            val hash = entity.toDomain().hash
            if(aux.containsKey(hash)){
                aux[hash] = aux.getValue(hash) + 1
            } else{
                aux[hash] = 1
            }
        }
        aux.keys.forEach { key ->
            out.add(Pair(key, aux.getValue(key)))
        }
        out.sortByDescending { it.second }
        return if(k < out.size){
            out.subList(0, k-1)
        } else {
            out
        }
    }

}

