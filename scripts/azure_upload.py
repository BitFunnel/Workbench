from azure.storage.blob import BlockBlobService
from azure.storage.blob import PublicAccess
from azure.storage.blob import ContentSettings
import os.path

def upload2(path):
    files = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and f.endswith('.tar.gz')]
    for f in files:
        print(f)


# BEFORE RUNNING: pip install azure-storage
# container is the name for the Azure blog storage container to create.
# path is the path to the directory containing .tar.gz files to upload.
def upload(path, container, account, key):
    block_blob_service = BlockBlobService(account_name=account, account_key=key)
    block_blob_service.create_container(container, public_access=PublicAccess.Container)
    files = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and f.endswith('.tar.gz')]
    for file in files:
        print("Uploading " + file)
        block_blob_service.create_blob_from_path(
            container,
            file,   # This is the path to the file on the local machine.
            file)   # TODO: This is the name for the blog on azure. Really want to strip off the first part of the path.


