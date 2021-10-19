"""
File Name  : common_functions.py
Class Name : CommonFunctions
Description: This program includes all those functions which are used frequently in other files. Functions like
             gdp_spark_session, get_scope,reading config_files (topic_config, metric_config, unify_config) are
             defined in this file.
Last Modified Date    : 06 August 2021
Changes               : Removed dependency from platform_keys_tbl
"""
import traceback
from functools import wraps
import inspect
import datetime
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode
from pyspark.sql.functions import current_timestamp



# noinspection PyMethodMayBeStatic
class CommonFunctions:

    def get_dbutils(self, spark):
        try:
            from pyspark.dbutils import DBUtils
            return DBUtils(spark)
        except Exception as e:
            message = "Get dbutils failed!{0}".format(e)
            error_trace = traceback.format_exc()
            data = {
                Constant.ACTIVITY_NAME: "get_dbutils",
                Constant.STATUS: Constant.FAILED_STATUS,
                Constant.ERROR_AND_TRACE: error_trace
            }
            Logger.error(message, data)
            raise e

    def gdp_spark_session(self):

        try:
            spark = SparkSession.builder.enableHiveSupport().getOrCreate()
            return spark
        except Exception as e:
            message = "Spark Session failed!{0}".format(e)
            error_trace = traceback.format_exc()
            data = {
                Constant.ACTIVITY_NAME: "gdp_spark_session",
                Constant.STATUS: Constant.FAILED_STATUS,
                Constant.ERROR_AND_TRACE: error_trace
            }
            Logger.error(message, data)
            raise e

    def get_scope(self, spark):

        try:
            dataframe_scope_tbl = spark.sql("select databricks_scope from databricks_scope_tbl")
            scope_tbl_value = dataframe_scope_tbl.collect()
            databricks_scope_value = scope_tbl_value[0].databricks_scope
            databricks_scope = databricks_scope_value
            return databricks_scope
        except Exception as e:
            message = "Get Data bricks scope failed!{0}".format(e)
            error_trace = traceback.format_exc()
            data = {
                Constant.ACTIVITY_NAME: "get_scope",
                Constant.STATUS: Constant.FAILED_STATUS,
                Constant.ERROR_AND_TRACE: error_trace
            }
            Logger.error(message, data)
            raise e

    def get_key_vault_databricks_scoped_secret_value(self, databricks_scope, secret_key):

        try:
            spark = CommonFunctions().gdp_spark_session()
            dbutils = CommonFunctions().get_dbutils(spark)
            secret_value = dbutils.secrets.get(scope=databricks_scope, key=secret_key)
            return secret_value
        except Exception as e:
            message = "Get platform keys  failed!{0}".format(e)
            error_trace = traceback.format_exc()
            data = {
                Constant.ACTIVITY_NAME: "get_key_vault_databricks_scoped_secret_value",
                Constant.STATUS: Constant.FAILED_STATUS,
                Constant.ERROR_AND_TRACE: error_trace
            }
            Logger.error(message, data)
            raise e

   