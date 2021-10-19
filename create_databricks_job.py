import ast
import json
import os
import sys
import time

class CreateACLJob:

    # This method polls the running Databricks job from Spark-Submit json and continues
    # running it till it receives a lifecycle state
    def poll_running_databricks_job(self, run_id, life_cycle_state, profile):
        print("poll_running_databricks_job:::::::")
        i = 1
        while i <= 30:
            print(f"poll_running_databricks_job:::::::while i={i}")
            current_job_status = os.popen(f"databricks runs get --run-id {run_id} --profile {profile}").read()
            current_job_status_dict = json.loads(current_job_status)
            life_cycle_state = current_job_status_dict["state"]["life_cycle_state"]
            #state_message = current_job_status_dict["state"]["state_message"]
            print(f"Life Cycle State is {life_cycle_state}")
            #print(f"State message is {state_message}")
            i = i + 1
            if life_cycle_state in ("SKIPPED","INTERNAL_ERROR"):
                print(f"Life Cycle State is {life_cycle_state}")
                print("Job failed")
                sys.exit(1)
                break

            if life_cycle_state == 'TERMINATED':
                result_state = current_job_status_dict["state"]["result_state"]
                print(f"Life Cycle State is {life_cycle_state}")
                print(f"Result State is {result_state}")
                if result_state == "FAILED":
                    sys.exit(1)
                break
            else:
                print("Job completed")
            time.sleep(30)
        print(i)
        if i == 31:
            print("Timed out, please check the job status in Dbricks view")
            sys.exit(1)

    # This method creates the Databricks job from Saprk-Submit json and executes the job.
    def execute_databricks_job(self, profile):
        #'cd scripts/'
        print("Inside execute_databricks_job:::")
        jsonFile = open("spark_submit_acl_job4.json", "r")  # Open the JSON file for reading
        data = json.load(jsonFile)
        print(data)
        job_id_json = os.popen(f"databricks jobs create --json-file 'spark_submit_acl_job4.json' --profile {profile}").read()
        print("job_id_json:::"+job_id_json)
        job_id = ast.literal_eval(job_id_json)["job_id"]
        print("Job ID is", job_id)
        run_id_json = os.popen(f"databricks jobs run-now --job-id {job_id} --profile {profile}").read()
        run_id = ast.literal_eval(run_id_json)["run_id"]
        print("Job Run ID is", run_id)
        current_job_status = os.popen(f"databricks runs get --run-id {run_id} --profile {profile}").read()
        current_job_status_dict = json.loads(current_job_status)
        print(current_job_status_dict)
        life_cycle_state = current_job_status_dict["state"]["life_cycle_state"]
        print("Life Cycle State of Job is", life_cycle_state)
        state_message = current_job_status_dict["state"]["state_message"]
        return run_id, life_cycle_state

    def update_acl_job_json_file(self,SPN,StorageAccName,TopicName,ContainerName,ADGroup,profile):
        #'cd jenkins_deploy/scripts/'
        print("Inside update_acl_job_json_file function")
        jsonFile = open("spark_submit_acl_job4.json", "r")  # Open the JSON file for reading
        data = json.load(jsonFile)  # Read the JSON into the buffer
        jsonFile.close()  # Close the JSON file
        # if SPN is not '':
        #     data["notebook_task"]["base_parameters"]["SPN"] = SPN
        data["notebook_task"]["base_parameters"]["StorageAccName"] = StorageAccName
        data["notebook_task"]["base_parameters"]["TopicName"] = TopicName
        data["notebook_task"]["base_parameters"]["ContainerName"] = ContainerName
        data["notebook_task"]["base_parameters"]["ADGroup"] = ADGroup 

        ## Save our changes to JSON file
        jsonFile = open("spark_submit_acl_job4.json", "w+")
        jsonFile.write(json.dumps(data))
        jsonFile.close()
        jsonFile = open("spark_submit_acl_job4.json", "r")  # Open the JSON file for reading
        data = json.load(jsonFile)
        print(data)
        run_id, lifecycle_state = self.execute_databricks_job(profile)
        self.poll_running_databricks_job(run_id, lifecycle_state, profile)


if __name__ == "__main__":
    print(str(sys.argv[1]), str(sys.argv[2]), str(sys.argv[3]),str(sys.argv[4]))
    CreateACLJob().update_acl_job_json_file(str(sys.argv[1]), str(sys.argv[2]), str(sys.argv[3]),str(sys.argv[4]),
                                                        str(sys.argv[5]))