class Machine < ActiveRecord::Base
  attr_accessible :machine_id, :name, :school_id, :ssl_key
  belongs_to :school
  has_many :update_records, :dependent => :destroy

  before_destroy { |record|
    Update.destroy_all("typ_id = #{record.id} AND typ=2")
    Purchase.destroy_all("typ_id = #{record.id} AND typ=2")
  }
  
  validates :name, :presence => true
  validates :machine_id, :presence => true, :uniqueness => true
  validates :ssl_key, :presence => true, :uniqueness => true
  validate :school_validity

  def school_validity
    if !school_id || !School.find(school_id)
      errors.add(:school_id, "Please choose a valid school for the machine.")
    end
  end
end
