package org.rootbr

import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.application.impl.EmbeddedProcessApplication
import org.camunda.bpm.container.RuntimeContainerDelegate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.jooby.Jooby.run
import org.jooby.Kooby


class JoobyCamundaApplication : Kooby({
    get {
        val engineService = BpmPlatform.getProcessEngineService()

        "Hello ${engineService.defaultProcessEngine.name}!"
    }



    get("/process-application") {
        val applicationService = BpmPlatform.getProcessApplicationService()
        "Hello ${applicationService.getProcessApplicationInfo("example-process-application").deploymentInfo.get(0).deploymentId}!" }


})

fun main(args: Array<String>) {
    Thread {
        val processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000")
                .setJobExecutorActivate(true)
                .buildProcessEngine()

        val runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get()
        runtimeContainerDelegate.registerProcessEngine(processEngine)

        val processApplication = ExampleProcessApplication();

        processApplication.deploy();

        processEngine.getRuntimeService().startProcessInstanceByKey("Process_13nmxyw");

    }.start()

    run(::JoobyCamundaApplication, args)
}

@ProcessApplication(name = "example-process-application", deploymentDescriptors = ["processes.xml"])
class ExampleProcessApplication : EmbeddedProcessApplication()
