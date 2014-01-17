from __future__ import print_function
from sqlalchemy import *
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base
import sys, os, subprocess, datetime, getpass

# Set default values (Preferred way => Enviornment variables)
repo_dir = os.getenv('REPO_DIR', '/home/git/repositories')
host = os.environ.get('DB_HOST')
database = os.environ.get('UPDATE_DB')
username = os.environ.get('DB_USER')
password = os.environ.get('DB_PASSWORD')

Base = declarative_base()
engine = create_engine("mysql://" + username + ":" + password + "@" + host + "/" + database + "?charset=utf8&use_unicode=0", pool_recycle=3600, echo=False)
Session = sessionmaker(bind=engine)
session = Session()

class Repository(Base):
  __tablename__ = 'repositories'

  id = Column(Integer, primary_key=True)
  name = Column(String)
  created_at = Column(DateTime, default=datetime.datetime.utcnow)
  updated_at = Column(DateTime, default=datetime.datetime.utcnow)

  def __init__(self, name, description):
    self.name = name

  def __repr__(self):
    return "<Repository('%s',%s','%s')>" % (self.name, self.created_at, self.updated_at)

class Comit(Base):
  __tablename__ = 'comits'

  id = Column(Integer, primary_key=True)
  repository_id = Column(Integer)
  comit_hash = Column(String)
  message = Column(String)
  is_update = Column(Integer)
  time = Column(DateTime)
  created_at = Column(DateTime, default=datetime.datetime.utcnow)
  updated_at = Column(DateTime, default=datetime.datetime.utcnow)

  def __init__(self, repository_id, comit_hash, message, is_update, time):
    self.repository_id = repository_id
    self.comit_hash = comit_hash
    self.message = message
    self.is_update = is_update
    self.time = time

  def __repr__(self):
    return "<Comit('%s','%s','%s','%s','%s')>" % (self.repository_id, self.comit_hash. self.message, self.is_update, self.time)

class Fil(Base):
  __tablename__ = 'fils'

  id = Column(Integer, primary_key=True)
  comit_id = Column(Integer)
  filename = Column(String)
  size = Column(Integer)
  action = Column(Integer)
  created_at = Column(DateTime, default=datetime.datetime.utcnow)
  updated_at = Column(DateTime, default=datetime.datetime.utcnow)

  def __init__(self, comit_id, filename, size, action):
    self.comit_id = comit_id
    self.filename = filename
    self.size = size
    self.action = action

  def __repr__(self):
    return "<Commit('%s','%s','%s','%s')>" % (self.comit_id, self.filename. self.size, self.action)

class DatabaseManager:

  def __init__(self, session):
    self.session = session

  def repo_exists(self, name):
    our_repo = self.session.query(Repository).filter_by(name=name).first()
    if our_repo:
      return True
    return False

  def get_repo(self, name):
    return self.session.query(Repository).filter_by(name=name).first().id

  def create_repo(self, name):
    new_repo = Repository(name, '')
    self.session.add(new_repo)
    session.commit()

  def add_comit(self, repository_id, commit_hash, message, time):
    new_commit = Comit(repository_id, commit_hash, message, 0, time)
    self.session.add(new_commit)
    session.commit()
    return self.session.query(Comit).filter_by(comit_hash=commit_hash).first().id

  def add_files(self, comit_id, action, files):
    # type = 0/1/2 Added/Modified/Deleted
    for f in files:
      filename = f[0]
      size = f[1]
      new_fil = Fil(comit_id, filename, size, action)
      self.session.add(new_fil)
      session.commit()

class FileHandler:

  def __init__(self, repo_dir, repository, commit_hash):
    self.repo_dir = repo_dir
    self.repository = repository
    self.commit_hash = commit_hash
    self.get_files()

  def __get_commit_files(self, command):
    files = []
    # change PWD to repository directory
    os.chdir(self.repo_dir + '/' + self.repository)
    
    # execute git commands
    self.process = subprocess.Popen("git show " + command + " --pretty=\"format:\" --name-only -r" + " " + self.commit_hash, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
     
    # Process the output here
    while self.process.poll() is None:
      output = self.process.stdout.readline()
      fs = filter(None, output.split('\n'))
      for f in fs:
        files.append(f)
    output = self.process.communicate()[0]
    fs = filter(None, output.split('\n'))
    for f in fs:
      files.append(f)
    return files

  def get_files(self):
    # Declare 3 lists Added/Modified/Deleted Files
    self.add_files, self.mod_files, self.del_files = [], [], []
    # Actions = Add/Edit/Delete
    actions = [ "", "--diff-filter=M", "--diff-filter=D" ]
    # Get all files for a commit
    self.files = self.__get_commit_files(actions[0])
    # Get Modified files for a commit
    self.m_files = self.__get_commit_files(actions[1])
    # Get Deleted files for a commit
    self.d_files = self.__get_commit_files(actions[2])
    # Create a new list for Added files, initialize it with all files
    self.a_files = list(self.files)
    # Remove all the files from aFiles that have been modified/deleted leaving only added files
    for f in self.files:
      if f in self.m_files or f in self.d_files:
        self.a_files.remove(f)
    # Mount the repository
    '''
    self.process = subprocess.Popen("git fs", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    message, err = self.process.communicate()
    message
    err
    '''
    # Get size of all added files
    for f in self.a_files:
      self.add_files.append([f, os.path.getsize(self.repo_dir + "/" + self.repository + "/fs/commits/" + self.commit_hash + "/worktree/" + f)])
    # Get new size of all modified files
    for f in self.m_files:
      self.mod_files.append([f, os.path.getsize(self.repo_dir + "/" + self.repository + "/fs/commits/" + self.commit_hash + "/worktree/" + f)])
    # Set size of all deleted files to zero
    for f in self.d_files:
      self.del_files.append([f, 0])

repo = sys.argv[1]
branch = sys.argv[2]
comit_hash = sys.argv[3]

if branch == 'master':

  # Change the PWD
  os.chdir(repo_dir + '/' + repo)

  # Clone the new revision
  p = subprocess.Popen("git clone " + getpass.getuser() + "@localhost:" + repo[:-4] + " " + repo_dir + "/" + repo + "/fs/commits/" + comit_hash + "/worktree", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  message, err = p.communicate()

  # Reset the files to this revision
  os.chdir(repo_dir + "/" + repo + "/fs/commits/" + comit_hash + "/worktree")
  p = subprocess.Popen("git reset --hard " + comit_hash, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  message, err = p.communicate()

  # Remove the GIT files from this folder
  p = subprocess.Popen("rm -Rf .git", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  message, err = p.communicate()

  # Change the PWD
  os.chdir(repo_dir + '/' + repo)

  # Get commit time
  p = subprocess.Popen("git show -s --format=\"%ci\" " + comit_hash, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  time, err = p.communicate()

  # Get commit message
  p = subprocess.Popen("git show --pretty=\"format:%B\" --name-only " + comit_hash, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  message, err = p.communicate()

  # Initialize DatabaseManager
  dbm = DatabaseManager(session)

  if dbm.repo_exists(repo) != True:
    dbm.create_repo(repo)

  # Get repository id
  repository_id = dbm.get_repo(repo)

  # Get generated commit id
  comit_id = dbm.add_comit(repository_id, comit_hash, message, time)

  # Get all files for a commit
  f = FileHandler(repo_dir, repo, comit_hash)

  # Create records for files
  dbm.add_files(comit_id, 0, f.add_files)
  dbm.add_files(comit_id, 1, f.mod_files)
  dbm.add_files(comit_id, 2, f.del_files)
