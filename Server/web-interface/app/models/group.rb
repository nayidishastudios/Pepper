class Group < ActiveRecord::Base
  attr_accessible :comments, :contact, :name
  has_many :schools, :dependent => :destroy

  before_destroy { |record|
    Update.destroy_all("typ_id = #{record.id} AND typ=0")
    Purchase.destroy_all("typ_id = #{record.id} AND typ=0")
  }

  validates :name, :presence => true
end
