import hudson.model.*
import jenkins.model.*
def DATABRICKS_DEV1_TOKEN = "pt-core-data-platform-databricks-dev-token"
def DATABRICKS_DEV2_TOKEN = "pt-core-data-platform-databricks-new-dev-token"
def DATABRICKS_STAGE1_TOKEN = "pt-core-data-platform-databricks-stage-token"
def DATABRICKS_STAGE2_TOKEN = "pt-core-data-platform-databricks-new-stage-token"
def DATABRICKS_STAGE3_TOKEN = "pt-core-data-platform-databricks-stage3-token"
def DATABRICKS_PROD_TOKEN = "pt-core-data-platform-databricks-prod-token"
def DATABRICKS_PREPROD_TOKEN = "pt-core-data-platform-databricks-preprod-token"

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
    descriptionPropertyValue: 'selectEnv,Dev1,Dev2,Stage1,Stage2,Stage3,Prod,PreProd',
    multiSelectDelimiter: ',',
    type: 'PT_SINGLE_SELECT',
    value: 'selectEnv,Dev1,Dev2,Stage1,Stage2,Stage3,Prod,PreProd',
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

//   choice(

//  name: 'Permissions',
//  choices: ['select permission','Read','Write'],
//  description : 'Enter the permission you want to set'
//   )
 
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
       
       stage('Installing tools'){
        steps {
        withEnv(["HOME=${env.WORKSPACE}"]) {
         sh script:'''
              python3 --version
              python3 -m pip install setuptools
              python3 -m pip install --upgrade pip --user
              python3 -m pip install databricks-cli
              python3 -m pip install jq
              echo $PATH
              export PATH="${WORKSPACE}/.local/bin:$PATH"
            '''
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
    // stage ("Extracting Team name and Producer AD group"){
    //         steps {
    //             script {
                  
    //               String a = "${params.ProducerADGroup}";
    //             //  print(a);
    //               String team_name = a.split("_")[0];
    //               String producer_AD_group= a.split("_")[1];
    //               env.team_name = team_name
    //               env.producer_AD_group = producer_AD_group

    //                print("Team Name : " + team_name);
    //                print("Producer AD Group : "+producer_AD_group); 
    //                     }
    //                }
    //       }

// stage('Forming Key Name') {
//               steps {
//                  script {
//                 String keyName = "producer"+team_name+Credential
//                 env.keyName = keyName
//                 print( "Key Name : "+keyName)
//               }
//               }
//     }

         stage("Read properties") {
      steps{
        script {
            def props = readProperties  file:'jenkins_job_certificate.properties'

		env.ProducerADGroup = params.ProducerADGroup
		env.Credential = params.Credential
		env.Value = params.Value
		env.Environment = params.Environment
        env.ConfigFilePath = props.ConfigFilePathForCertificateGeneration
		// env.CertificateName = keyName
		// env.AkvSecretName = keyName
		env.JobIdDev1 = props.JobIdDev1
		env.JobIdDev2 = props.JobIdDev2
		env.JobIdStage1 = props.JobIdStage1
		env.JobIdStage2 = props.JobIdStage2
		env.JobIdStage3 = props.JobIdStage3
		env.JobIdProd = props.JobIdProd
		env.JonIdPreProd = props.JonIdPreProd


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
          
   stage('Adding Files to Key Vault') {
		//  when{
		// 	expression { Credential == 'KeystoreFile' || Credential == 'TruststoreFile' }
		//  }
              steps { 
		
		      script {
		      if("${params.Environment}" =='Dev1') 
          {
			DATABRICKS_TOKEN = "${DATABRICKS_DEV1_TOKEN}"
			env.jobId = env.JobIdDev1
          }
      if("${params.Environment}" =='Dev2')
      {
			DATABRICKS_TOKEN = "${DATABRICKS_DEV2_TOKEN}"
			env.jobId = env.JobIdDev2
      }
			if("${params.Environment}" =='Stage1') 
      {
			DATABRICKS_TOKEN = "${DATABRICKS_STAGE1_TOKEN}"
			env.jobId = env.JobIdStage1
      }
      if("${params.Environment}" =='Stage2')
        {
			DATABRICKS_TOKEN = "${DATABRICKS_STAGE2_TOKEN}"
			env.jobId = env.JobIdStage2
        }
      if("${params.Environment}" =='Stage3')
        {
			DATABRICKS_TOKEN = "${DATABRICKS_STAGE3_TOKEN}"
			env.jobId = env.JobIdStage3
        }
			if("${params.Environment}" =='Prod') 
      {
			DATABRICKS_TOKEN = "${DATABRICKS_PROD_TOKEN}"
			env.jobId = env.JobIdProd
			}
      if("${params.Environment}" =='PreProd') 
      {
			DATABRICKS_TOKEN = "${DATABRICKS_PREPROD_TOKEN}"
			env.jobId = env.JobIdPreProd
			}
		      }
	      }
}  

stage('Setting ACL for AD Group & SPN')
     {
              steps { 
		
		      script {
            
			executeDbricksJob()
			            }
		      }
	}

}
}

def executeDbricksJob() {
   withCredentials([ usernamePassword(credentialsId: "${DATABRICKS_TOKEN}",
                                    usernameVariable: 'DATABRICKS_URL',
                                    passwordVariable: 'DATABRICKS_ACCESS_TOKEN')
                               ]) {
                    sh script:"""
                    #!/bin/bash
		      export PATH="${WORKSPACE}/.local/bin:$PATH"
                         export PATH="/home/jenkins/.local/lib/python3.8/site-packages:$PATH"
                        #chmod 777 Jenkins/scripts/configure_databricks.sh
                         #sh Jenkins/scripts/configure_databricks.sh ${DATABRICKS_URL} ${DATABRICKS_ACCESS_TOKEN}
                         > ~/.databrickscfg
                         echo "[${params.Environment}]" >> ~/.databrickscfg
                         echo "host = ${DATABRICKS_URL}" >> ~/.databrickscfg
                         echo "token = ${DATABRICKS_ACCESS_TOKEN}" >> ~/.databrickscfg
                         echo "" >> ~/.databrickscfg
                         cat ~/.databrickscfg
                         export PATH="${WORKSPACE}/.local/bin:$PATH"
                         echo "Connection Successful"
                         databricks --version
                         databricks --version --profile ${params.Environment}
                         echo "version step completed"
                         python3 create_databricks_job.py "${env.SPN}" "${env.StorageAccountName}" "${env.ContainerName}_${env.TopicName}" "${env.ContainerName}" "${env.ADGroup}" ${env.Environment}
                    """
                               }
                               }

