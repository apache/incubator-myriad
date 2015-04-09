# Myriad Fine Grained Scaling

The objective of "Fine Grained Scaling" is to bring elasticity of resources between Myriad/YARN and other Mesos frameworks. The idea is to allow YARN to take a resource offer from mesos, run enough containers (YARN tasks) that the offer can hold and release the offer once the containers finish.

### The feature (currently) works as follows

* Admin launches Node Managers via [Myriad flexup API](API.md), but with **zero** capacities.
* The Node Managers report **zero** capacities to the Resource Manager upon registration.
* A user submits an application to YARN (for e.g. a MapReduce job).
* The application is added to RM's scheduling pipeline. However, YARN scheduler (for e.g. FairShareScheduler) cannot schedule the application containers as all the Node Managers have zero available capacity.
* The Myriad Mesos Framework (running inside RM) subsequently receives resource offers from Mesos.
* An offer (from a slave node that's already running a NM) is projected to YARN scheduler as "available capacity" on the Node Manager.
* The YARN scheduler now goes ahead and allocates containers (tasks) for the Node Managers.
* For each allocated container, Myriad framework spins up a "placeholder" mesos task. (Usually, a bunch of "placeholder" tasks are launched for a single mesos offer.)
* NMs become aware of container allocations via YARN's HB mechanism. But Myriad ensures that NMs are made aware of container allocations only after the corresponding "placeholder" mesos tasks are launched.
* When NMs report to RM that some of the containers have "finished", Myriad sends out "finished" status updates to Mesos for the corresponding "placeholder" tasks.
* Mesos takes back the resources from Myriad that were previously blocked using "placeholder" tasks.

### To try out..

* [Build myriad](myriad-dev.md) with a special "zero" profile added to myriad-config-default.yml:
```
profiles:
  zero:
    cpu: 0
    mem: 0
```
* Spin up Resource Manager with "Myriad Scheduler" plugged into it.
* Flexup a few NMs using [/api/cluster/flexup](API.md), but with **"zero"** profile:
```
{
  "instances":3, "profile": "zero"
}
```
* The Node Managers advertise **zero** resources to Resource Manager (RM's UI should show this).
* Submit a M/R job to the Resource Manager.
* The Mesos UI should show "placeholder" mesos tasks (prefixed with "yarn_") for each yarn container. 
* The job should finish successfully (although the NMs were originally launched with 0 capacities).
* The placeholder mesos tasks should finish as and when the YARN containers finish.

### Sample Screenshot

![mesos_tasks_for_yarn_containers](https://cloud.githubusercontent.com/assets/3505177/7049736/d7995bf8-ddd0-11e4-850d-c59bca1fd1bf.png)
