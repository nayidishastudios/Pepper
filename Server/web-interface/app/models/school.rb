class School < ActiveRecord::Base
  attr_accessible :comments, :contact, :group_id, :name
  belongs_to :group
  has_many :machines, :dependent => :destroy

  before_destroy { |record|
    Update.destroy_all("typ_id = #{record.id} AND typ=1")
    Purchase.destroy_all("typ_id = #{record.id} AND typ=1")
  }
  
  validates :name, :presence => true
  validate :group_validity

  def group_validity
    if !group_id || !Group.find(group_id)
      errors.add(:group_id, "Please choose a valid group for the school.")
    end
  end
end
