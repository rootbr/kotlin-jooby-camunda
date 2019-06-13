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
import org.jooby.json.Gzon

data class Dto(val testString: String)
class JoobyCamundaApplication : Kooby({

    use(Gzon())

    onStart {
        val processEngine = (ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                as StandaloneInMemProcessEngineConfiguration).apply {
            processEnginePlugins.add(SpinProcessEnginePlugin())
            defaultSerializationFormat = Variables.SerializationDataFormats.JSON.name
            databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
            jdbcUrl = "jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000"
            setJobExecutorActivate(true)
        }.buildProcessEngine()
        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine)
        ExampleProcessApplication().deploy()
    }

    get {
        val runtimeService = BpmPlatform.getDefaultProcessEngine().runtimeService
        val processInstance = runtimeService.startProcessInstanceByKey("Process_13nmxyw")
        runtimeService.setVariable(processInstance.processInstanceId, "anyVariable", JSON("{\"val\":\"var\"}"))
        runtimeService.getVariable(processInstance.processInstanceId, "anyVariable")
//        "Process Engine: ${BpmPlatform.getDefaultProcessEngine().name}"
    }

    get("/task") { BpmPlatform.getDefaultProcessEngine().taskService.createTaskQuery().list() }

})

fun main(args: Array<String>) {
    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
