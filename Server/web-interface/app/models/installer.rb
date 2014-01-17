class Installer < ActiveRecord::Base
  attr_accessible :contact, :name, :password, :username
  has_secure_password

  validates :username, :presence => true, :format => { :with => /^(|(([A-Za-z0-9]+_+)|([A-Za-z0-9]+\-+)|([A-Za-z0-9]+\.+)|([A-Za-z0-9]+\++))*[A-Za-z0-9]+@((\w+\-+)|(\w+\.))*\w{1,63}\.[a-zA-Z]{2,6})$/i, :on => :create }, :uniqueness => true
  validates :name, :presence => true, :length => { :maximum => 30 }
  validates :password, :confirmation => true, :length => { :minimum => 8, :maximum => 20 }
  before_save { |user| user.username = username.downcase }
end
