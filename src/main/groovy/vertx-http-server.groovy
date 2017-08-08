import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Field
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

def options = new VertxOptions();
options.setFileResolverCachingEnabled(false);
def vertx = Vertx.vertx(options)
def server = vertx.createHttpServer()
def router = Router.router(vertx)
router.route().handler(BodyHandler.create())
@Field Logger logger = LoggerFactory.getLogger("http-server")

router.route(HttpMethod.GET, "/").handler({ routingContext ->
    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
    routingContext.response().end("ok\n")
})

router.route(HttpMethod.POST, "/request").handler({ routingContext ->
    logger.info("REQUEST: ${routingContext.getBodyAsString()}")
    def slurper = new JsonSlurper()
    def requestMap = slurper.parseText(routingContext.getBodyAsString())
    def responseString = requestMap.user?.buyeruid
    if (responseString == null) {
        responseEndNoContent(routingContext.response())
        return
    }
    def responseMap = slurper.parseText(responseString)
    if (!(responseMap instanceof Map)) {
        responseEndNoContent(routingContext.response())
        return
    }
    def response = routingContext.response()
    if (responseMap.containsKey("sleep")) {
        handleSleep(vertx, requestMap, responseMap, response)
    } else {
        addAltHeadersToResponseIfAny(response, responseMap)
        sendNoContentIfNoBid(response, responseMap)
        handleNormalRequest(requestMap, responseMap, response)
    }
})

private handleSleep(vertx, requestMap, responseMap, response) {
    vertx.setTimer(responseMap.sleep, { it ->
        addAltHeadersToResponseIfAny(response, responseMap)
        sendNoContentIfNoBid(response, responseMap)
        handleNormalRequest(requestMap, responseMap, response)
    })
}

private addAltHeadersToResponseIfAny(response, responseMap) {
        responseMap.alt_headers?.each { key, value -> response.putHeader(key, value) }
}

private sendNoContentIfNoBid(response, responseMap) {
    if (responseMap.containsKey("no-data")) {
        responseEndNoContent(response)
    }
}

private responseEndNoContent(response) {
    response.setStatusCode(204)
    response.end()
    logger.info("RESPONSE: No Content")
}

private handleNormalRequest(requestMap, responseMap, response) {
    if (response.ended())
        return
    responseMap.id = requestMap.id
    response.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
    def responseJSON = JsonOutput.toJson(responseMap)
    response.end(responseJSON)
    logger.info("RESPONSE: ${responseJSON}")
}

server.requestHandler(router.&accept).listen(8888)
