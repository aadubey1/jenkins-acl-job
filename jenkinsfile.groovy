import hudson.model.*
import jenkins.model.*



pipeline {

  //Update the custom docker agent in Jenkins file in the below section
  agent {
        label 'pipes-docker-agent'
  }
  
  options {
        disableConcurrentBuilds()
    }

parameters {

 choice(name: 'Producer', choices: ['Select prod','Producer1',
   'Producer2',
   'Producer3',
    'Producer4',
    'Producer5', 
    'Producer6'],  description: 'Select Producer')

  extendedChoice(
    name: 'Environment', defaultValue: 'None',
    descriptionPropertyValue: 'selectEnv,Dev1,Dev2,Stage1,Stage2,Stage3,Prod',
    multiSelectDelimiter: ',',
    type: 'PT_SINGLE_SELECT',
    value: 'selectEnv,Dev1,Dev2,Stage1,Stage2,Stage3,Prod',
    visibleItemCount: 4, description: 'Select Environment'
    )

    extendedChoice(name: 'ProducerADGroup', defaultValue: '',
  descriptionPropertyValue: 'Select Option,'+
                            'Bf(Azure-GDP-Producer-BF-NonProd),'+
                            'Clickstream(Azure-GDP-Producer-Clickstream-Dev),'+
                            'Flex(Azure-GDP-Compute-NonProd),'+
                            'Ga(Azure-GDP-Producer-Assortment-Stage),'+
                            'Gpr(Azure-GDP-Producer-GprSvc-Dev),'+
                            'Mds(Azure-ProfileResolution-Stage),'+
                            'Ngf(Azure-GDP-Producer-FinFxr-Nonprod),'+
                            'OmsInvoice(Azure-GDP-Producer-OMS-Stage),'+
                            'OmsOrder(Azure-GDP-Producer-OMS-Stage),'+
                            'OmsRelease(Azure-GDP-Producer-OMS-Stage),'+
                            'Pc73(Azure-GDP-Producer-PriceServices-Dev),'+
                            'Pf(Azure-GDP-Producer-PF-NonProd),'+
                            'PlanConfig(Azure-GDP-Producer-PlanConfig-Dev),'+
                            'Resa(Azure-GDP-Producer-Resa-Stage),'+
                            'Rms(Azure-GDP-Producer-InventoryNonProd),'+
                            'Spex(Azure-GDP-Producer-SPEX-Dev),'+
                            'Stibo(Azure-GDP-Producer-StiboGapIncLocations)',
  multiSelectDelimiter: ',',type: 'PT_SINGLE_SELECT',
  value: 'selectOption,'+
            'Bf_Azure-GDP-Producer-BF-NonProd,'+
            'Clickstream_Azure-GDP-Producer-Clickstream-Dev,'+
            'Flex_Azure-GDP-Compute-NonProd,'+
            'Ga_Azure-GDP-Producer-Assortment-Stage,'+
            'Gpr_Azure-GDP-Producer-GprSvc-Dev,'+
            'Mds_Azure-ProfileResolution-Stage,'+
            'Ngf_Azure-GDP-Producer-FinFxr-Nonprod,'+
            'OmsInvoice_Azure-GDP-Producer-OMS-Stage,'+
            'OmsOrder_Azure-GDP-Producer-OMS-Stage,'+
            'OmsRelease_Azure-GDP-Producer-OMS-Stage,'+
            'Pc73_Azure-GDP-Producer-PriceServices-Dev,'+
            'Pf_Azure-GDP-Producer-PF-NonProd,'+
            'PlanConfig_Azure-GDP-Producer-PlanConfig-Dev,'+
            'Resa_Azure-GDP-Producer-Resa-Stage,'+
            'Rms_Azure-GDP-Producer-InventoryNonProd,'+
            'Spex_Azure-GDP-Producer-SPEX-Dev,'+
            'Stibo_Azure-GDP-Producer-StiboGapIncLocations',
  visibleItemCount: 3, description: 'Select Producer AD Group')

  choice(

 name: 'Permissions',
 choices: ['select permission','Read','Write'],
 description : 'Enter the permission you want to set'
  )
 
  string(
          name: 'ADGroup', 
            trim: true,
            description: 'Enter the AD Group'
        )

  string(
          name: 'StorageAccountName', 
            trim: true,
            description: 'Enter name of storage account'
        )
  string(
          name: 'ContainerName', 
            trim: true,
            description: 'Enter Domain(container) name'
        )
  string(
          name: 'TopicName', 
            trim: true,
            description: 'Enter folder(topic) name'
        )

  string(
          name: 'SPN', 
            trim: true,
            description: 'Enter the SPN'
        )

}
 
 
stages {


 
    stage('Producer Name') {
      steps {
          script{
            a= env.Producer
            echo a
          }

      }
    }

       stage('AD Group(entered)') {
      steps {
          script{ 
            b = env.ADGroup
            echo b
          }

      }
    }


     stage('User AD Groups') {
      steps {
          script{
            wrap([$class: 'BuildUser']) {
    
            println("User AD Groups : " + env.BUILD_USER_GROUPS)
        }
          
      }

    }
    }


     stage('SPN Name') {
      steps {
          script{
            c = env.SPN
            echo c
          }

      }
    }

stage('Validate AD Groups') {
	            steps {
	                script {
	                    wrap([$class: 'BuildUser']) {
	                        def userGroups = env.BUILD_USER_GROUPS
	                        
	                        def userSplit = userGroups.split(',')
	                        
	                            if( userSplit.find{e-> e.equalsIgnoreCase(ProducerADGroup)} ){
	                               println "User ${env.BUILD_USER_ID} is a member of ${ProducerADGroup}"
	                               }
	                                else{
	                                println "User ${env.BUILD_USER_ID} is not a member of ${ProducerADGroup} "
	                                 
	                                // error "This pipeline stops here!"
	                                
	                            }
	
	                    }
	                }
	            }
	        }
          
    stage('Setting ACL for AD Group'){
        steps{
            sh script:"""
           
            pip install azure-storage-file-datalake --pre
          
             pip install azure-identity
             pip install azure-keyvault-secrets
           
            python3 acl_for_default_access.py "${ADGroup}" "${StorageAccountName}" "${ContainerName}" "${TopicName}" "${SPN}"
            echo "The ACL for the AD Group has been set"
          """  
        }
    }



    stage('Setting ACL for SPN')
      {
        steps
        {
            script
            {
              if(SPN)
              {
              sh script:"""
             python3 acl_for_default_access.py 
            echo "The ACL for the SPN has been set"
            """
              }
              else
              echo "Not setting the ACL for SPN"
            }
        }
      }



  }
}