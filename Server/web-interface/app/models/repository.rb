class Repository < ActiveRecord::Base
  attr_accessible :name
  has_many :comits, :dependent => :destroy
  has_many :updates, :dependent => :destroy
  has_many :purchases, :dependent => :destroy

  validates :name, :presence => true, :length => { :maximum => 255 }
end
