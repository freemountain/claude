package org.freemountain.operator.dtos;

public class DataStoreProviderConfig {
    public static class JobSpec {
        private String image;
        private String entry;

        public JobSpec() {
        }

        public JobSpec(String image, String entry) {
            this.image = image;
            this.entry = entry;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getEntry() {
            return entry;
        }

        public void setEntry(String entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return "JobSpec{" +
                    "image='" + image + '\'' +
                    ", entry='" + entry + '\'' +
                    '}';
        }
    }


    private String name;
    private String password;
    private String username;
    private String host;
    private String port;
    private JobSpec job = new JobSpec();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public JobSpec getJob() {
        return job;
    }

    public void setJob(JobSpec job) {
        this.job = job;
    }


    @Override
    public String toString() {
        return "DataStoreConfig{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", job=" + job +
                '}';
    }
}
