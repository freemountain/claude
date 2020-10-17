

### JobHandler

## on JobFinished (Lifecycle.MODIFIED && scucceded)
Resource                Job thinks                  

spec changed            spec changed               do nothing
spec not changed        spec not changed           add ready(scucceded) condition and reconcile(owner)
spec changed            spec not changed           job adds ready condition, add ready condition would fail -> do nothing
spec not changed        spec changed               do nothing -> ?? Would this ever happen / maybe a bug

## addReadyCondition(ownerResource, ownerHash, success , transitionTime )
updateCOndition
    if failed 
        spec changed  -> do noting
        meta data changed -> get current resource state and retry
        status changed -> get current resource state and retry if last Ready condition transition time is before transitionTime


## onJobDeletedBeforeFinished(Lifecycle.DELETED && not finished)
Resource                Job thinks                  

spec changed            spec changed               do nothing
spec not changed        spec not changed           reconcile(owner)

spec changed            spec not changed           reconcile -> a job would run twice -> dont care
spec not changed        spec changed               do nothing -> like no job is run for current resource state ->  we need a periodically reconcile(ALL)


### ResourceHandler

onSpecChanged(T resource)
    spawn job