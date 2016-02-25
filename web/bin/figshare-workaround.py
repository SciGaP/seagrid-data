#!/usr/bin/env python
#This is a workaround script as I couldn't get this working using php
import json
import os
import hashlib
import urllib2
import sys
import requests

data = json.loads(sys.argv[1])

BASE_URL = 'https://api.figshare.com/v2/{endpoint}'
TOKEN = '9a60beb20f4c58bc84b28c4f6774b151a3a9a1f548821d94a7a950b734dc03f05bca3ff1395787b7f7aa4683910d1bb1bb7a64ea27a13761dd68c29a9cfd9985'
FILE_PATH = data[0]
#FILE_PATH = '/Users/supun/Downloads/default_job.out';
ARTICLE_ID = data[1]
#ARTICLE_ID = '2782744';

CHUNK_SIZE = 1048576


def raw_issue_request(method, url, data=None):
    headers = {'Authorization': 'token ' + TOKEN}
    if data is not None:
        data = json.dumps(data)
    response = requests.request(method, url, headers=headers, data=data)
    try:
        response.raise_for_status()
        try:
            data = json.loads(response.content)
        except ValueError:
            data = response.content
    except urllib2.HTTPError as error:
        print 'Caught an HTTPError: {}'.format(error.message)
        print 'Body:\n', response.content
        raise

    return data


def issue_request(method, endpoint, *args, **kwargs):
    return raw_issue_request(method, BASE_URL.format(endpoint=endpoint), *args, **kwargs)



def get_file_check_data(file_name):
    with open(file_name, 'rb') as fin:
        md5 = hashlib.md5()
        size = 0
        data = fin.read(CHUNK_SIZE)
        while data:
            size += len(data)
            md5.update(data)
            data = fin.read(CHUNK_SIZE)
        return md5.hexdigest(), size


def initiate_new_upload(article_id, file_path):
    endpoint = 'account/articles/{}/files'
    endpoint = endpoint.format(article_id)

    md5, size = get_file_check_data(file_path)
    data = {'name': os.path.basename(file_path),
            'md5': md5,
            'size': size}

    result = issue_request('POST', endpoint, data=data)

    result = raw_issue_request('GET', result['location'])

    return result


def complete_upload(article_id, file_id):
    issue_request('POST', 'account/articles/{}/files/{}'.format(article_id, file_id))


def upload_parts(file_info):
    url = '{upload_url}'.format(**file_info)
    result = raw_issue_request('GET', url)

    with open(FILE_PATH, 'rb') as fin:
        for part in result['parts']:
            upload_part(file_info, fin, part)


def upload_part(file_info, stream, part):
    udata = file_info.copy()
    udata.update(part)
    url = '{upload_url}/{partNo}'.format(**udata)

    stream.seek(part['startOffset'])
    data = stream.read(part['endOffset'] - part['startOffset'] + 1)

    raw_issue_request('PUT', url, data=data)

file_info = initiate_new_upload(ARTICLE_ID, FILE_PATH)
upload_parts(file_info)
complete_upload(ARTICLE_ID, file_info['id'])
