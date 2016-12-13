import bz2
import os.path
import re
import subprocess
import urllib.request               # TODO: should be able to remove now.
import urllib
from urllib.parse import urlparse   # TODO: just use fully qualified name.


def load_from_url(url):
    print('Fetching ' + url)
    headers = { 'User-Agent' : 'Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)' }
    request = urllib.request.Request(url=url, headers = headers)
    response = urllib.request.urlopen(request)
    text = response.read().decode('utf-8')
    return text


def ensure_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)


def run(working_directory, command):
    proc = subprocess.Popen(command, cwd = working_directory,  stdout = subprocess.PIPE, stderr = subprocess.PIPE)
    output, error = proc.communicate();
    returncode = proc.wait()
    print("Output = " + str(output))
    print("Error = " + str(error))
    print("Return code = " + str(proc.returncode))


class Corpus:
    def __init__(self, root, url, wikiextractor_path):
        self.root = root
        self.raw = os.path.join(root, "raw")
        self.dump_page = os.path.join(self.raw, "dump_page.html")
        self.extracted = os.path.join(root, "extracted")
        self.chunks = os.path.join(root, "chunks")
        self.url = url
        self.wikiextractor = wikiextractor_path


    def initialize(self):
        ensure_directory(self.raw)
        if os.path.exists(self.dump_page):
            print(self.url + " already downloaded.")
        else:
            print("Downloading " + self.url + " ==> " + self.dump_page)
            urllib.request.urlretrieve(self.url, self.dump_page)

        file = open(self.dump_page)
        text = file.read()
        hrefs = re.findall("href=\"(.*enwiki-\d{8}-pages-articles\d+.xml-.*\.bz2)\"", text);
        self.dumps = [(urllib.parse.urljoin(self.url, x),
                       re.search("enwiki-\d{8}-pages-articles\d+", x).group(0))
                      for x in hrefs]
        self.dumps = [self.dumps[0]]
        for x in self.dumps:
            print(x)


    def download(self):
        self.initialize()
        print("Download")
        ensure_directory(self.raw)
        for url, base_name in self.dumps:
            file = os.path.join(self.raw, base_name) + '.bz2'
            if os.path.exists(file):
                print(file + " already downloaded.")
            else:
                print("Downloading " + url + " ==> " + file)
                urllib.request.urlretrieve(url, file)


    def decompress(self):
        self.download()
        print("Decompress")
        for url, base_name in self.dumps:
            file = os.path.join(self.raw, base_name)
            if os.path.exists(file):
                print(file + " already decompressed.")
            else:
                print("Decompressing " + file + '.bz2')
                input_path = file + '.bz2'
                output_path = file
                with open(output_path, 'wb') as output, bz2.BZ2File(input_path, 'rb') as input:
                    for data in iter(lambda: input.read(100 * 1024), b''):
                        output.write(data)


    def wikiextract(self):
        self.decompress()
        print("wikiextract")
        ensure_directory(self.extracted)
        for url, base_name in self.dumps:
            input = os.path.join(self.raw, base_name)
            output = os.path.join(self.extracted, base_name)
            if os.path.exists(output):
                print(output + " already WikiExtracted.")
            else:
                print("WikiExtracting " + input + " ==> " + output)
                run(self.raw, [self.wikiextractor,
                               input,
                               '-o',
                               output,
                               '--lists'])


    def chunk(self):
        self.wikiextract()
        print("chunk")
        ensure_directory(self.chunks)
        for url, base_name in self.dumps:
            input = os.path.join(self.extracted, base_name)
            output = os.path.join(self.chunks, base_name)
            if os.path.exists(output):
                print(output + " already chunked.")
            else:
                # TODO: Implement chunking
                print("TODO: Chunking " + input + " ==> " + output)
                # run(self.raw, [self.wikiextractor,
                #                input,
                #                '-o',
                #                output,
                #                '--lists'])


    def compress_for_azure(self):
        self.chunk()
        # TODO: Implement compress_for_azure
        print("TODO: Compress for Azure")


    def upload_to_azure(self):
        self.compress_for_azure()
        # TODO: Implement upload_to_azure
        print("TODO: Upload to Azure")


url = "https://dumps.wikimedia.org/enwiki/20161120/"
root = "/home/mhop/wikipedia"
wikiextractor = "/home/mhop/git/wikiextractor/WikiExtractor.py"

corpus = Corpus(root, url, wikiextractor)
#corpus.initialize()
#corpus.download()
#corpus.decompress()
#corpus.wikiextract()
corpus.upload_to_azure()