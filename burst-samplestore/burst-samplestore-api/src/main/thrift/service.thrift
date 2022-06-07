namespace * org.burstsys.samplestore.api

include "types.thrift"


/*
 * the sample store thrift service endpoint
 */
service BurstSampleStoreApiService {

   /*
    * get a list of data transfer endpoints for a specific view definition.
    */
    types.BurstSampleStoreApiViewGenerator getViewGenerator(
        1: required string guid // global operation uid
        2: required types.BurstSampleStoreDataSource dataSource
    )

}

