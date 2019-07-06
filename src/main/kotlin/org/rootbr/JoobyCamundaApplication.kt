package org.rootbr

import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.application.impl.EmbeddedProcessApplication
import org.camunda.bpm.container.RuntimeContainerDelegate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.Spin.JSON
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import org.jooby.Jooby.run
import org.jooby.Kooby
import org.jooby.Results
import org.jooby.handlers.CorsHandler
import org.jooby.pebble.Pebble
import org.rootbr.camunda.spin.gson.GsonNode


data class Dto(val testString: String)
class JoobyCamundaApplication : Kooby({

    onStart {
        val processEngine = (ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                as StandaloneInMemProcessEngineConfiguration).apply {
            processEnginePlugins.add(SpinProcessEnginePlugin())
            defaultSerializationFormat = Variables.SerializationDataFormats.JSON.name
            databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
//            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=1000"
            jdbcUrl = "jdbc:h2:tcp://localhost/~/tmp/h2dbs/camunda-h2-dbs/process-engine;MVCC=TRUE;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE"
            setJobExecutorActivate(true)
        }.buildProcessEngine()
        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine)
        org.rootbr.ProcessApplication().deploy()
        val runtimeService = BpmPlatform.getDefaultProcessEngine().runtimeService
        val processInstance = runtimeService.startProcessInstanceByKey("Process_13nmxyw")
    }

    use(Pebble())
    use("*", CorsHandler())

    get("/") { _ -> Results.html("index") }

    assets("/**/*.js")
    assets("/index.html")
    assets("/*.bpmn")

    get("/api") {
        val json = JSON("{\"val\":\"var\"}")
        val taskService = BpmPlatform.getDefaultProcessEngine().taskService
        val task = taskService.createTaskQuery().list().first()

        taskService.setVariable(task.id, "jsonVariable5", json)
        val variable = taskService.getVariable(task.id, "jsonVariable4") as GsonNode
        variable.prop("val").value()
    }

    post("/engine-rest/deployment/create") {
        //TODO
    }.consumes("json")

    post("/api/message/{messageName}") { request ->
        val messageName = request.param("messageName").value()
        val businessKey = request.param("businessKey").value()
        val body = request.body(String::class.java)
        BpmPlatform.getDefaultProcessEngine().runtimeService.correlateMessage(messageName, businessKey, mapOf(messageName to JSON(body)))
    }.consumes("json").produces("json")

    get("/api/activities") {
        val taskService = BpmPlatform.getDefaultProcessEngine().taskService
        val historyService = BpmPlatform.getDefaultProcessEngine().historyService
        JSON(taskService.createTaskQuery().list().groupingBy { it.taskDefinitionKey }.eachCount()).toString()
    }
    get("/api/history-activities") {
        val historyService = BpmPlatform.getDefaultProcessEngine().historyService
        JSON(historyService.createHistoricActivityInstanceQuery().list().groupingBy { it.activityId }.eachCount()).toString()
    }

})

fun main(args: Array<String>) {
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "process-application", deploymentDescriptors = ["processes.xml"])
class ProcessApplication : EmbeddedProcessApplication()
