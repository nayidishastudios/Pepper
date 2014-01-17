class User < ActiveRecord::Base
  attr_accessible :email, :name, :password
  has_secure_password

  validates :email, :presence => true, :format => { :with => /^(|(([A-Za-z0-9]+_+)|([A-Za-z0-9]+\-+)|([A-Za-z0-9]+\.+)|([A-Za-z0-9]+\++))*[A-Za-z0-9]+@((\w+\-+)|(\w+\.))*\w{1,63}\.[a-zA-Z]{2,6})$/i, :on => :create }, :uniqueness => true
  validates :name, :presence => true, :length => { :maximum => 30 }
  validates :password, :confirmation => true, :length => { :minimum => 8, :maximum => 20 }
  before_save { |user| user.email = email.downcase }
  before_save :create_remember_token

  private
  def create_remember_token
    self.remember_token = SecureRandom.urlsafe_base64
  end
end
