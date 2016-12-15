import bz2
import os.path
import re
import subprocess
import tarfile
import urllib.request
import urllib.parse


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
    proc = subprocess.Popen(command,
                            cwd = working_directory,
                            stdout = subprocess.PIPE,
                            stderr = subprocess.PIPE)
    output, error = proc.communicate();
    returncode = proc.wait()
    print("Output = " + str(output))
    print("Error = " + str(error))
    print("Return code = " + str(proc.returncode))


class Corpus:
    def __init__(self, root, url, wikiextractor_repo, workbench_repo):
        self.root = os.path.abspath(root)
        self.raw = os.path.join(root, "raw")
        self.dump_page = os.path.join(self.raw, "dump_page.html")
        self.extracted = os.path.join(root, "extracted")
        self.chunks = os.path.join(root, "chunks")
        self.url = url
        self.wikiextractor = os.path.join(wikiextractor_repo, "WikiExtractor.py")
        self.workbench = os.path.join(workbench_repo, "target", "corpus-tools-1.0-SNAPSHOT.jar")


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
        # TODO: Stop truncating self.dumps. Currently truncating to limit the amount of
        # files downloaded and processed during development.
        # self.dumps = self.dumps[:2]
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
                               '--lists',
                               '--s',
                               '--filter_disambig_pages'])


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
                print("Chunking " + input + " ==> " + output)
                run(self.chunks,
                    ["java",
                     "-cp",
                     self.workbench,
                     "org.bitfunnel.workbench.MakeCorpusFile",
                     input,
                     output])


    def compress(self):
        self.chunk()
        print("Compress")
        for url, base_name in self.dumps:
            input = os.path.join(self.chunks, base_name)
            output = input + ".tar.gz"
            if os.path.exists(output):
                print(input + " already compressed.")
            else:
                print("Compressing " + input + " ==> " + output)
                tar = tarfile.open(output, "w:gz")

                for path, dirs, files in os.walk(input):
                    for filename in files:
                        filepath = os.path.join(path, filename)
                        arcname = os.path.relpath(filepath, self.chunks)
                        print(arcname)
                        tar.add(filepath, arcname=arcname)
                tar.close()

    # [os.path.relpath(os.path.join(path, file), os.path.join(root, "chunks", "")) for path, dirs, files in
    #  os.walk(os.path.join(root, "chunks", "enwiki-20161120-pages-articles1")) for file in files]


    def upload_to_azure(self):
        self.compress()
        # TODO: Implement upload_to_azure
        print("TODO: Upload to Azure")


url = "https://dumps.wikimedia.org/enwiki/20161120/"
#root = "/home/mhop/wikipedia"
root = "/data"
#wikiextractor = "/home/mhop/git/wikiextractor/WikiExtractor.py"
wikiextractor = "/Users/michaelhopcroft/git/wikiextractor"
workbench = "/Users/michaelhopcroft/git/Workbench"


corpus = Corpus(root, url, wikiextractor, workbench)
#corpus.initialize()
#corpus.download()
#corpus.decompress()
#corpus.wikiextract()
corpus.upload_to_azure()

# Install Python 3.5.2
# pip3.5 install azure-storage
