# Myriad Fine Grained Scaling

The objective of "Fine Grained Scaling" (FGS) is to bring elasticity of resources between Myriad/YARN and other Mesos frameworks.
The idea is to allow YARN to take resource offers from Mesos, run enough containers (YARN tasks) that the offers can hold and release the resources back to Mesos once the containers finish.

* Node Managers that register with RM with (0 memory,0 cpu) are eligible for FGS. i.e. Myriad expands/shrinks the capacity of such NMs with the resources offered by Mesos. Further, Myriad ensures that YARN containers will be launched on such NMs only if Mesos offers enough resources on the slave nodes running those NMs.
* A new **zero** profile is defined in [myriad-config-default.yml](../myriad-scheduler/src/main/resources/myriad-config-default.yml#L15) to help admins launch NMs with (0mem,0cpu) capacities using the [/api/cluster/flexup](API.md#put-apiclusterflexup).
* Node Managers that register with RM with more than (0 mem, 0 cpu) are **not** eligible for FGS. i.e. Myriad will not expand/shrink the capacity of such NMs. These NMs are typically launched with low/medium/high profile.

### The feature (currently) works as follows

* Admin launches Node Managers via [Myriad flexup API](API.md#put-apiclusterflexup) with **zero** capacities.
* The Node Managers report **zero** capacities to the Resource Manager upon registration.
* A user submits an application to YARN (for e.g. a MapReduce job).
* The application is added to RM's scheduling pipeline. However, YARN scheduler (for e.g. FairShareScheduler) will not allocate any application containers on the **zero** profile Node Managers. 
* If there are other Node Managers that were registered with RM using non-zero capacities (low/medium/high profiles), some containers might be allocated for those NMs depending on their free capacity.
* Myriad subsequently receives resource offers from Mesos for slave nodes running **zero** profile NMs.
* The offered resources are projected to YARN's scheduler as "available capacity" of the **zero** profile Node Manager. For e.g. if Mesos offers (10G,4CPU) for a given node, then the capacity of the **zero** profile NM running on that node increases to (10G,4CPU).
* The YARN scheduler now goes ahead and allocates a few containers for the **zero** profile Node Managers.
* For each allocated container, Myriad spins up a "placeholder" Mesos task that holds on to Mesos resources as long as the corresponding YARN container is alive.
* (In reality, a bunch of "placeholder" tasks are launched in a single shot, corresponding to a bunch of containers YARN allocates.)
* NMs become aware of container allocations via YARN's HB mechanism. Myriad ensures that NMs are made aware of container allocations only after the corresponding "placeholder" Mesos tasks are launched.
* When NMs report to RM that some of the containers have "finished", Myriad sends out "finished" status updates to Mesos for the corresponding "placeholder" tasks.
* Mesos takes back the resources from Myriad that were held using "placeholder" tasks upon receiving the "finished" status updates.

### To try out Fine Grained Scaling

* Spin up Resource Manager with "Myriad Scheduler" plugged into it.
* Flexup a few NMs using [/api/cluster/flexup](API.md) with **"zero"** profile:
```
{
  "instances":3, "profile": "zero"
}
```
* The **zero** profile Node Managers advertise **zero** resources to Resource Manager (RM's "Nodes" UI should show this).
* Submit a M/R job to the Resource Manager.
* When Mesos offers resources to Myriad, the Mesos UI should show "placeholder" mesos tasks (prefixed with "yarn_") for each yarn container allocated using those offers.
* The RM's UI should show these containers allocated to the **zero** profile NM nodes.
* The placeholder mesos tasks should finish as and when the YARN containers finish.
* The job should finish successfully (although some NMs were originally launched with 0 capacities).


### Sample Screenshot

![mesos_tasks_for_yarn_containers](https://cloud.githubusercontent.com/assets/3505177/7049736/d7995bf8-ddd0-11e4-850d-c59bca1fd1bf.png)
