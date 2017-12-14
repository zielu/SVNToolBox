FROM elleflorio/svn-server

RUN ["htpasswd", "-b", "/etc/subversion/passwd", "admin", "password"]
RUN mkdir /home/svn/repos
RUN ["svnadmin", "create", "/home/svn/repos/repo"]
COPY conf /home/svn/repos/repo/conf
COPY project /import/project/