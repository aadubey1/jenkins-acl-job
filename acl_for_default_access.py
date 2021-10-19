import adal
import requests
import sys
import ast
import os
import time
import json
from azure.common.credentials import ServicePrincipalCredentials
from msrestazure.azure_active_directory import AADTokenCredentials

from azure.storage.filedatalake import DataLakeServiceClient
from azure.identity import ClientSecretCredential





class SetAcl:

    def get_object_id_for_adgroup( authentication_endpoint, tenant_id, resource_graph, client_id, client_secret, graph_api_version, producer_ad_group):
                  
        url= resource_graph + "/" + graph_api_version+ "/groups?$search=" + '"displayName:' + producer_ad_group + '"'
        
        context = adal.AuthenticationContext(authentication_endpoint + tenant_id)

        token_response = context.acquire_token_with_client_credentials(resource_graph, client_id, client_secret) 
        headers = {'Authorization': 'Bearer ' + token_response ['accessToken'], 'Content-Type': 'application/json',  'ConsistencyLevel': 'eventual'}

        r = requests.get(url, headers=headers)

        json_dump = json.dumps(r.json(), indent=4, separators= (',', ': ')) 
        json_load = json.loads(json_dump)
       
        count = len(json_load['value'])
        for i in range(count):
             if json_load ['value'][i]['displayName'] == producer_ad_group: 
                 #print(json_load ['value'][i]['displayName'])
                 object_id = json_load['value'][i]['id'] 
                 #print (object_id)
        return object_id

    def get_object_id_for_spn( authentication_endpoint, tenant_id, resource_graph, client_id, client_secret, graph_api_version, producer_spn):      
        url= 'https://graph.microsoft.com/v1.0/servicePrincipals?$count=true&$search=' + '"displayName:' + producer_spn + '"' + '&$select=id,appId,appDisplayName'
        context = adal.AuthenticationContext(authentication_endpoint + tenant_id)

        token_response = context.acquire_token_with_client_credentials(resource_graph, client_id, client_secret) 
        headers = {'Authorization': 'Bearer ' + token_response ['accessToken'], 'Content-Type': 'application/json',  'ConsistencyLevel': 'eventual'}

        r = requests.get(url, headers=headers)

        json_dump = json.dumps(r.json(), indent=4, separators= (',', ': ')) 
        json_load = json.loads(json_dump)
        count = len(json_load['value'])
        
        for i in range(count):
            if json_load ['value'][i]['appDisplayName'] == producer_spn: 
              #print(json_load['value'][i])
              object_id = json_load['value'][i]['id'] 
        return object_id



    def set_acl(tenant_id, client_id, client_secret, storage_account_name, domain_name, topic_name,object_id):
        acl = "default:group:" + object_id + ":r-x"
        credential = ClientSecretCredential(tenant_id, client_id, client_secret)
        service_client = DataLakeServiceClient(
            account_url="{}://{}.dfs.core.windows.net".format("https", storage_account_name), credential=credential)
        file_system_client = service_client.get_file_system_client(file_system=domain_name) 
        directory_client = file_system_client.get_directory_client(topic_name)
        directory_client.update_access_control_recursive(acl=acl)
        acl_props = directory_client.get_access_control()
        #print (acl_props['acl'])
        #print(credential)
        return acl_props


    if __name__=="__main__":
        authentication_endpoint='https://login.microsoftonline.com/'
        tenant_id='348a1296-55b6-466e-a7af-4ad1a1b79713' #need this
        resource_graph="https://graph.microsoft.com"  
        client_id='810894b4-8501-4d5e-967c-bfe8309630ee'   #need this
        client_secret= '163f2a8a-c9a6-48f5-8973-190c841e36cc'  #need this
        graph_api_version="v1.0"
        
        
        # producer_ad_group="Azure-GDP-DataLake-NonProd"
        producer_ad_group= str(sys.argv[1])
        storage_account_name = str(sys.argv[1]) #need this
        domain_name= str(sys.argv[2]) #need this
        topic_name = str(sys.argv[3]) #need this
        producer_spn = str(sys.argv[4])

        obj_id_adgroup = get_object_id_for_adgroup( authentication_endpoint, tenant_id, resource_graph, client_id, client_secret, graph_api_version, producer_ad_group)
        acl_prop_adgroup = set_acl(tenant_id, client_id, client_secret, storage_account_name, domain_name, topic_name, obj_id_adgroup)
        print(obj_id_adgroup)
        print('=======')
        print(acl_prop_adgroup)

        if(producer_spn):
            obj_id_spn = get_object_id_for_spn( authentication_endpoint, tenant_id, resource_graph, client_id, client_secret, graph_api_version, producer_ad_group)
            acl_prop_spn = set_acl(tenant_id, client_id, client_secret, storage_account_name, domain_name, topic_name, obj_id_spn)
        
            print(obj_id_spn)
            print('=======')
            print(acl_prop_spn)


