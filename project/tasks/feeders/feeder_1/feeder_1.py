# coding=utf-8
from __future__ import division
from pymongo import MongoClient
from pathlib import Path
# logging library -> https://docs.python.org/2/library/logging.html?highlight=datefmt#logrecord-attributes
import logging
import os
import xml.etree.ElementTree
import json


# constants definition
# .. global config
FILE_GLOBAL_CONFIG = "../../../global_config.xml"
CFG_EMAIL_ADMIN = "email_admin"
CFG_LOG_PATH = "log_path"
CFG_LOG_LEVEL = "log_level"
CFG_OFF_SOURCE_HOST = "off_source_host"
CFG_OFF_SOURCE_PORT = "off_source_port"
# .. local config
FILE_LOCAL_CONFIG = "../config_feeders.xml"
CFG_PATH_TO_ROOT = "path_to_root"
CFG_OUT_PATH = "out_path"
CFG_OUT_ALL_PRODUCTS = "out_file_feeder_1_all_products"
CFG_OUT_OK_FEEDER_1 = "out_file_ok_feeder_1"
CFG_OUT_OK_FEEDER_2 = "out_file_ok_feeder_2"

P_NAME = "feeder_1"
MAX_ALL_PRODUCTS = 1000000


def preconditions_fulfilled():
    """
    If preconditions are fulfilled, then the current process may proceed
    :return: True/False
    """
    # ok file feeder_1
    fname_ok_feeder_1 = get_val_local_conf(CFG_OUT_OK_FEEDER_1)
    fok_1 = "%s/%s/%s" % (get_val_local_conf(CFG_PATH_TO_ROOT), get_val_local_conf(CFG_OUT_PATH), fname_ok_feeder_1)
    path_ok_feeder_1 = Path(fok_1)
    return not path_ok_feeder_1.is_file()


def log_activation():
    log_pathname_folder = "%s/%s" % (get_val_local_conf(CFG_PATH_TO_ROOT), get_val_global_conf(CFG_LOG_PATH))
    log_path_folder = Path(log_pathname_folder)
    if not log_path_folder.exists():
        os.makedirs(log_pathname_folder)

    fname_log = "%s/%s.log" % (log_pathname_folder, P_NAME)
    log_level_value = get_val_global_conf(CFG_LOG_LEVEL).upper()
    log_level = getattr(logging, log_level_value, None)
    line_template = "%(asctime)-15s [%(TASK)s] %(levelname)s: %(message)s"
    global d
    d = {'TASK': P_NAME}
    logging.basicConfig(filename=fname_log, level=log_level, format=line_template)
    logging.info("Log Level is %s" % log_level_value, extra=d)


def get_val_global_conf(a_key):
    conf_glob = xml.etree.ElementTree.parse(FILE_GLOBAL_CONFIG).getroot()
    return conf_glob.find(a_key).text


def get_val_local_conf(a_key):
    conf_local = xml.etree.ElementTree.parse(FILE_LOCAL_CONFIG).getroot()
    return conf_local.find(a_key).text


# *********
# START
# *********

# activate logging capabilities
log_activation()

# check if process needs to be launched
if not preconditions_fulfilled():
    logging.info("no need to feed further the chain. Last feed is still in progress.", extra=d)
    exit(0)

# connect to db
logging.info("connecting to server MongoClient", extra=d)
pongo = MongoClient(get_val_global_conf(CFG_OFF_SOURCE_HOST), int(get_val_global_conf(CFG_OFF_SOURCE_PORT)))
logging.info(".. connecting to OPENFOODFACTS database", extra=d)
db = pongo["off"]
logging.info(".. getting PRODUCTS collection", extra=d)
coll_products = db["products"]
logging.info("%d products are referenced" % (coll_products.find().count()), extra=d)

# extract all products based on projection fields
# .. filter on relevant fields
fields_projection = {"_id": 1,
                     "code": 1,
                     "generic_name": 1,
                     "product_name": 1,
                     "countries_tags": 1,
                     "categories_tags": 1,
                     "nutriments": 1,
                     "brands_tags": 1,
                     "stores_tags": 1,
                     "images": 1}
# .. retrieve all products
#cursor_products = coll_products.find({"categories_tags": {"$exists": True, "$not": {"$size": 0}}}, fields_projection)
cursor_products = coll_products.find({}, fields_projection)
# save extraction in json format
pathname_folder = "%s/%s" % (get_val_local_conf(CFG_PATH_TO_ROOT), get_val_local_conf(CFG_OUT_PATH))
path_folder = Path(pathname_folder)
fname_all_products = "%s/%s" % (pathname_folder, get_val_local_conf(CFG_OUT_ALL_PRODUCTS))
path_all_products = Path(fname_all_products)
if path_folder.exists():
    if path_all_products.is_file():
        os.remove(fname_all_products)
else:
    os.makedirs(pathname_folder)

f_all_products = open(fname_all_products, "a")
i = 0
f_all_products.write('[')
for product in cursor_products:
    if "code" in product:
        product_json = json.dumps(product, indent=4)
        if i > 0:
            f_all_products.write(",")
        
        f_all_products.write(product_json)
        i = i + 1
        #if i >= MAX_ALL_PRODUCTS:
        #    break

f_all_products.write("]")
f_all_products.close()
print "[%s].. closing connection" % P_NAME
pongo.close()
print "[%s]connection closed." % P_NAME

# unlock next processes: create ok file (empty file)
fname_ok = get_val_local_conf(CFG_OUT_OK_FEEDER_1)
fok = "%s/%s" % (pathname_folder, fname_ok)
path_ok = Path(fok)
if not path_ok.is_file():
    f_ok = open(fok, "w")
    f_ok.close()
else:
    # ERROR: should have been deleted by task preparer (precondition for current task also)
    logging.error("feeding process went to completion but file '%s' exists and should not." % (fname_ok), extra=d)
